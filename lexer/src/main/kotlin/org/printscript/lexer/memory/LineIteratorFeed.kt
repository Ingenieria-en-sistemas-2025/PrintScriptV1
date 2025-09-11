package org.printscript.lexer.memory

private const val DEFAULT_LINES_PER_CHUNK = 64
private const val DEFAULT_MAX_WINDOW_CAPACITY = 1 shl 15 // 32 KiB de ventana

@Suppress("TooManyFunctions") // esto vuela, mientras tanto para commits desp arreglo
class LineIteratorFeed(
    private val lineIterator: Iterator<String>,
    private val insertNewlineBetweenLines: Boolean = true,
    private val linesPerChunk: Int = DEFAULT_LINES_PER_CHUNK,
    private val maxWindowCapacity: Int = DEFAULT_MAX_WINDOW_CAPACITY,

) : CharFeed { // ver temas de mutabilidad

    // Estado de la ventana
    private var windowBuffer = CharArray(maxWindowCapacity)
    private var windowStartAbsIndex = 0
    private var windowLength = 0
    private var endOfInputReached = false

    // Estado de linea pendiente
    private var pendingLine: String? = null
    private var pendingOffset: Int = 0

    // pin para proteger inicio de token en la compactacion
    private var pinnedFromAbsIndex: Int? = null

    // helpers de ventada
    private inline fun windowEndAbs(): Int = windowStartAbsIndex + windowLength
    private inline fun needToCover(absIndex: Int): Boolean = absIndex >= windowEndAbs()
    private inline fun hasSpace(): Boolean = windowLength < maxWindowCapacity
    private inline fun linesLeft(): Boolean = pendingLine != null || lineIterator.hasNext()

    override fun ensureAvailable(absIndex: Int): Boolean {
        if (absIndex < windowStartAbsIndex) return false
        if (!needToCover(absIndex)) return true
        refillUntilCovers(absIndex)
        return !needToCover(absIndex)
    }

    override fun charAt(absIndex: Int): Char? {
        if (!ensureAvailable(absIndex)) return null
        return windowBuffer[absIndex - windowStartAbsIndex]
    }

    override fun fixedSlice(startAbs: Int, len: Int): CharSequence {
        ensureAvailable(startAbs + len - 1)
        val availableLen = availableLengthFrom(startAbs, len)
        return FixedSlice(this, startAbs, availableLen)
    }

    override fun rollingSlice(startAbs: Int): CharSequence =
        RollingSlice(this, startAbs)

    override fun eofFrom(absIndex: Int): Boolean {
        if (ensureAvailable(absIndex)) return false
        if (absIndex < windowStartAbsIndex) return false
        return endOfInputReached
    }

    private fun refillUntilCovers(absIndexNeeded: Int) {
        if (endOfInputReached) return
        var loadedFullLines = 0

        while (!endOfInputReached && needToCover(absIndexNeeded)) {
            if (!hasSpace()) compactWindow()
            val appended = refillOnce(loadedFullLines)
            if (appended == 0) {
                // No pudimos agregar nada: o no hay más datos o estamos “pinned” sin progreso.
                if (!linesLeft()) endOfInputReached = true
                break
            }
            if (pendingLine == null && appended > 0 && lastAppendWasFullLine) {
                loadedFullLines++
                if (loadedFullLines >= linesPerChunk) break
            }
        }
    }

    private var lastAppendWasFullLine: Boolean = false

    private fun refillOnce(loadedFullLines: Int): Int {
        lastAppendWasFullLine = false
        var appended = 0
        // hay pendiente
        if (pendingLine != null && hasSpace()) {
            appended += copyFromPending()
            appended += maybeAppendNewlineWhenPendingFinished()
        }

        // no hay pendiente
        if (pendingLine == null && canStartNewLine(loadedFullLines)) {
            startNextLine()
            appended += copyFromPending()
            appended += maybeAppendNewlineWhenPendingFinished()
        }
        return appended
    }

    private fun canStartNewLine(loadedFullLines: Int): Boolean =
        lineIterator.hasNext() && hasSpace() && loadedFullLines < linesPerChunk

    private fun startNextLine() {
        var line = lineIterator.next()
        if (line.indexOf('\r') >= 0) line = line.replace("\r", "")
        pendingLine = line
        pendingOffset = 0
    }

    private fun maybeAppendNewlineWhenPendingFinished(): Int {
        if (pendingLine == null && insertNewlineBetweenLines && hasSpace()) {
            windowBuffer[windowLength++] = '\n'
            lastAppendWasFullLine = true
            return 1
        }
        return 0
    }

    private fun copyFromPending(): Int {
        val s = pendingLine ?: return 0
        val remaining = s.length - pendingOffset
        if (remaining <= 0) {
            pendingLine = null
            pendingOffset = 0
            return 0
        }
        val canCopy = (maxWindowCapacity - windowLength).coerceAtMost(remaining)
        for (i in 0 until canCopy) {
            windowBuffer[windowLength + i] = s[pendingOffset + i]
        }
        windowLength += canCopy
        pendingOffset += canCopy

        if (pendingOffset >= s.length) {
            pendingLine = null
            pendingOffset = 0
        }
        return canCopy
    }

    private fun compactWindow() {
        // Mantener al menos desde "pinned" si existe; si no, mitad final.
        val idealKeepFromAbs = windowStartAbsIndex + windowLength / 2
        val mandatoryKeepFromAbs = pinnedFromAbsIndex ?: idealKeepFromAbs
        val keepFromAbs = maxOf(windowStartAbsIndex, minOf(idealKeepFromAbs, mandatoryKeepFromAbs))
        val keepCount = windowEndAbs() - keepFromAbs
        if (keepCount <= 0) {
            windowStartAbsIndex = keepFromAbs
            windowLength = 0
            return
        }
        val offset = keepFromAbs - windowStartAbsIndex
        for (i in 0 until keepCount) windowBuffer[i] = windowBuffer[offset + i]
        windowStartAbsIndex = keepFromAbs
        windowLength = keepCount
    }

    private fun availableLengthFrom(startAbs: Int, desiredLen: Int): Int {
        val ok = ensureAvailable(startAbs + desiredLen - 1)
        if (!ok) {
            val available = (windowEndAbs() - startAbs).coerceAtLeast(0)
            return minOf(available, desiredLen)
        }
        return desiredLen
    }

    override fun pin(minAbsIndex: Int) {
        pinnedFromAbsIndex = pinnedFromAbsIndex?.let { minOf(it, minAbsIndex) } ?: minAbsIndex
    }

    override fun unpin(minAbsIndex: Int) {
        if (pinnedFromAbsIndex == minAbsIndex) pinnedFromAbsIndex = null
    }

    // char sequences views

    private class FixedSlice(
        val src: LineIteratorFeed,
        val startAbs: Int,
        val len: Int,
    ) : CharSequence {
        override val length: Int get() = len
        override fun get(index: Int): Char {
            require(index in 0 until len) { "index out of bounds" }
            return src.charAt(startAbs + index)
                ?: throw IndexOutOfBoundsException("FixedSlice: índice ${startAbs + index} ya no está disponible")
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

    private class RollingSlice(
        val src: LineIteratorFeed,
        val startAbs: Int,
    ) : CharSequence {
        override val length: Int
            get() {
                // Longitud "viva": hasta donde llega la ventana ahora.
                val windowEndExclusive = src.windowStartAbsIndex + src.windowLength
                return (windowEndExclusive - startAbs).coerceAtLeast(0)
            }
        override fun get(index: Int): Char {
            val abs = startAbs + index
            return src.charAt(abs)
                ?: throw IndexOutOfBoundsException("RollingSlice: EOF o compactado en abs=$abs")
        }
        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            require(startIndex in 0..endIndex)
            return src.fixedSlice(startAbs + startIndex, endIndex - startIndex)
        }
        override fun toString(): String {
            val lenNow = length
            val sb = StringBuilder(lenNow)
            for (i in 0 until lenNow) sb.append(get(i))
            return sb.toString()
        }
    }
}
