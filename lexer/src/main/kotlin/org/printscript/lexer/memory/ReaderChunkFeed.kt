package org.printscript.lexer.memory

import java.io.Reader

// Explico la idea general: (por tema memoria): Usamos un buffer fijo con x tamano, ventana deslizante,
// indices que el scanner y el tokenizer usan son absolutos por ende se traducen aca a indices relativos del buffer
// Cuando falta espacio o necesitamos ver mas lejos, se llena y si hace falta se compacta, los pines
// para asegurarnos que no se compacte ningun lexema.

class ReaderChunkFeed(
    private val reader: Reader,
    private val maxWindowCapacity: Int = DEFAULT_MAX_WINDOW_CAPACITY,
    private val chunkSize: Int = DEFAULT_CHUNK_SIZE,
    private val keepTail: Int = DEFAULT_KEEP_TAIL,
) : CharFeed { // 8 KiB

    companion object {
        private const val KIB: Int = 1024

        // Ventana total: 32 KiB
        const val DEFAULT_MAX_WINDOW_CAPACITY: Int = 32 * KIB

        // Lecturas parciales: 8 KiB
        const val DEFAULT_CHUNK_SIZE: Int = 8 * KIB

        // Cola mínima que conservamos en compaction: 8 KiB
        const val DEFAULT_KEEP_TAIL: Int = 8 * KIB
    }

    private var buffer = CharArray(maxWindowCapacity) // ventana
    private var startAbs = 0 // índice absoluto del primer char visible en buffer[0]
    private var len = 0 // cantidad de chars válidos en buffer[0..len)
    private var eof = false // si ya no queda nada por leer del Reader

    private var pinnedFromAbsIndex: Int? = null

    private inline fun endAbs(): Int = startAbs + len
    private inline fun hasSpace(): Boolean = len < maxWindowCapacity
    private fun relativeIndex(abs: Int): Int = abs - startAbs // de absoluto → índice en buffer

    override fun ensureAvailable(absIndex: Int): Boolean {
        if (absIndex < startAbs) return false // ya fue compactado, no está más
        if (absIndex < endAbs()) return true // ya está en la ventana
        refillUntilCovers(absIndex) // intentar traerlo
        return absIndex < endAbs()
    }

    // char at si puede, rellena hasta cubrir ese índice y luego devuelve el char.
    override fun charAt(absIndex: Int): Char? {
        if (!ensureAvailable(absIndex)) return null
        return buffer[relativeIndex(absIndex)]
    }

    // fixed slice -> intenta cubrir exactamente len chars desde startAbs.
    override fun fixedSlice(startAbs: Int, len: Int): CharSequence {
        if (len <= 0) return ""
        pin(startAbs)
        try {
            ensureAvailable(startAbs + len - 1)

            val windowStart = this.startAbs
            val windowEnd = endAbs()

            val available = if (startAbs < windowStart) 0 else (windowEnd - startAbs).coerceIn(0, len)
            if (available == 0) return ""

            val sb = StringBuilder(available)
            var abs = startAbs
            repeat(available) {
                val ch = charAt(abs++) ?: return@repeat
                sb.append(ch)
            }
            return sb.toString()
        } finally {
            unpin(startAbs)
        }
    }

    // Es una estructura viva: su length crece a medida que entra más data
    override fun rollingSlice(startAbs: Int): CharSequence =
        RollingSlice(this, startAbs)

    override fun eofFrom(absIndex: Int): Boolean {
        if (ensureAvailable(absIndex)) return false
        if (absIndex < startAbs) return false
        return eof
    }

    override fun pin(minAbsIndex: Int) {
        pinnedFromAbsIndex = pinnedFromAbsIndex?.let { minOf(it, minAbsIndex) } ?: minAbsIndex
    }

    override fun unpin(minAbsIndex: Int) {
        if (pinnedFromAbsIndex == minAbsIndex) pinnedFromAbsIndex = null
    }

    private fun refillUntilCovers(absNeeded: Int) {
        if (eof) return
        while (!eof && absNeeded >= endAbs()) {
            if (!hasSpace()) compact()
            if (!hasSpace()) break

            val appended = readChunk()
            if (appended <= 0) {
                eof = true
                break
            }
        }
    }

    private fun readChunk(): Int {
        val room = maxWindowCapacity - len
        if (room <= 0) return 0
        val toRead = minOf(room, chunkSize) // Lee como máximo chunkSize o lo que entre en la ventana.
        val read = reader.read(buffer, len, toRead) // lee mas caracteres, los pone en el buffer desde len (ultimo char)
        if (read > 0) len += read
        return read
    }

    // No realoca, no agranda el array: mantiene uso acotado de memoria
    private fun compact() {
        val end = endAbs()
        val pin = pinnedFromAbsIndex ?: end
        val tail = end - keepTail

        val anchor = kotlin.math.min(pin, tail) // mantené al menos keepTail caracteres recientes. no podemos recortar más allá del pin, ni “comernos” la cola protegida.
        val keepFromAbs = kotlin.math.max(startAbs, anchor)

        val keepCount = end - keepFromAbs // cuántos chars quedan.
        if (keepCount <= 0) {
            startAbs = keepFromAbs
            len = 0
            return
        }
        val offset = keepFromAbs - startAbs
        System.arraycopy(buffer, offset, buffer, 0, keepCount)
        startAbs = keepFromAbs
        len = keepCount
    }

    private class FixedSlice(
        val src: ReaderChunkFeed,
        val startAbs: Int,
        val len: Int,
    ) : CharSequence {
        override val length: Int get() = len

        // llama a charAt(startAbs + i) (volverá a intentar rellenar si hace falta). Si el char ya no está (compactación posterior), lanza IndexOutOfBoundsException.
        override fun get(index: Int): Char {
            require(index in 0 until len) { "index out of bounds" }
            return src.charAt(startAbs + index)
                ?: throw IndexOutOfBoundsException("FixedSlice: abs=${startAbs + index} fuera de ventana/EOF")
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            require(startIndex in 0..endIndex && endIndex <= len)
            return FixedSlice(src, startAbs + startIndex, endIndex - startIndex)
        }
        override fun toString(): String {
            val sb = StringBuilder(len)
            for (i in 0 until len) sb.append(get(i))
            return sb.toString()
        }
    }

    // Longitud dinámica (length = endAbs() - startAbs en cada consulta).
    private class RollingSlice(
        val src: ReaderChunkFeed,
        val startAbs: Int,
    ) : CharSequence {
        override val length: Int
            get() = (src.endAbs() - startAbs).coerceAtLeast(0)
        override fun get(index: Int): Char {
            val abs = startAbs + index
            return src.charAt(abs)
                ?: throw IndexOutOfBoundsException("RollingSlice: abs=$abs fuera de ventana/EOF")
        }
        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            require(startIndex in 0..endIndex)
            return src.fixedSlice(startAbs + startIndex, endIndex - startIndex)
        }
        override fun toString(): String {
            val n = length
            val sb = StringBuilder(n)
            for (i in 0 until n) sb.append(get(i))
            return sb.toString()
        }
    }
}
