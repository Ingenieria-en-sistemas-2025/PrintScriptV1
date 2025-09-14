import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.config.LexerFactory
import org.printscript.lexer.memory.ReaderChunkFeed
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReaderChunkFeedTest {

    @Test
    fun refillAndCompact_withoutPin_discardsHead() {
        val text = "abcdefghij" // 10 chars
        val feed = ReaderChunkFeed(
            reader = StringReader(text),
            maxWindowCapacity = 6, // ventana bien chica
            chunkSize = 4, // leer de a 4
            keepTail = 2, // conservar 2 sin pin
        )

        // Fuerzo a cargar los primeros 6: indices 0..5
        assertTrue(feed.ensureAvailable(5))
        // Avanzo la ventana pidiendo más allá de la capacidad para forzar compact
        assertTrue(feed.ensureAvailable(8))

        // Ya no debería estar el índice 0: fue compactado
        assertFalse(feed.ensureAvailable(0))
        // Sí debería poder leerse 8
        assertEquals('i', feed.charAt(8))
    }

    @Test
    fun pinPreventsDiscard_untilUnpin() {
        val text = "0123456789ABCDEFGHIJ" // 20 chars
        val feed = ReaderChunkFeed(
            reader = StringReader(text),
            maxWindowCapacity = 10,
            chunkSize = 5,
            keepTail = 2,
        )

        // Cargar primeros 10 (0..9)
        assertTrue(feed.ensureAvailable(9))

        // Pineo el índice 3 (protejo desde 3)
        feed.pin(3)

        // Pedimos cubrir 12 (FACTIBLE con capacidad 10 y pin=3)
        assertTrue(feed.ensureAvailable(12))

        // El 3 sigue accesible por el pin
        assertEquals('3', feed.charAt(3))

        // Libero pin
        feed.unpin(3)

        // Ahora podemos empujar hasta el final y dejar que 3 quede fuera
        assertTrue(feed.ensureAvailable(19))
        assertNull(feed.charAt(3))
    }

    @Test
    fun fixedSlice_returnsPartialAtEOF_insteadOfExploding() {
        val text = "hello"
        val feed = ReaderChunkFeed(StringReader(text), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1)
        // Pedimos slice desde 3 con len 5 (3..7), pero solo hay hasta 4 → devuelve 2 chars
        val slice = feed.fixedSlice(startAbs = 3, len = 5)
        assertEquals(2, slice.length)
        assertEquals("lo", slice.toString())
    }

    @Test
    fun rollingSlice_grows_andMayThrowIfHeadEvicted() {
        val text = "abcdef"
        val feed = ReaderChunkFeed(StringReader(text), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1)

        val rs = feed.rollingSlice(0)
        assertTrue(rs.length in 0..4) // crece a medida que entra data
        assertTrue(feed.ensureAvailable(5)) // obliga a compactar cabeza (cap=4)

        // Como la cabeza (0..) fue desalojada, toString() puede fallar:
        assertFailsWith<IndexOutOfBoundsException> { rs.toString() }

        assertFailsWith<IndexOutOfBoundsException> { rs.get(999) }
    }

    @Test
    fun eofFrom_semantics() {
        val text = "xyz"
        val feed = ReaderChunkFeed(StringReader(text), maxWindowCapacity = 2, chunkSize = 2, keepTail = 1)

        // Aún no en EOF para 0
        assertFalse(feed.eofFrom(0))
        assertTrue(feed.ensureAvailable(2)) // cubre índice 2 ('z')
        // Pedir más allá no cubre => EOF
        assertTrue(feed.eofFrom(3))
        // Pero para un índice atrás, no es EOF
        assertFalse(feed.eofFrom(1))
    }

    @Test
    fun pinImpossible_returnsFalse_withoutEOF() {
        val feed = ReaderChunkFeed(StringReader("0123456789"), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1)
        assertTrue(feed.ensureAvailable(3)) // carga algo
        feed.pin(0) // pin en 0
        // cubrir 6 con cap=4 es imposible con pin=0
        assertFalse(feed.ensureAvailable(6))
        // No debería marcar EOF (queda input por delante, solo falta espacio)
        assertFalse(feed.eofFrom(6))
        // Y el 0 sigue sin poder leerse porque nunca entró de nuevo
        assertNull(feed.charAt(6))
    }

    @Test
    fun scanner_positions_across_chunk_boundaries() {
        val text = "ab\ncd\nef" // 8 chars, 2 saltos de línea
        val feed = ReaderChunkFeed(StringReader(text), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1)
        val s0 = org.printscript.lexer.Scanner(feed)
        // avanza forzando refill/compact entre medias
        val s1 = s0.advance(3) // después de "ab\n"
        assertEquals(org.printscript.common.Position(2, 1), s1.position())
        val s2 = s1.advance(2) // "cd"
        assertEquals(org.printscript.common.Position(2, 3), s2.position())
        val s3 = s2.advance(1) // '\n'
        assertEquals(org.printscript.common.Position(3, 1), s3.position())
    }

    @Test
    fun lex_token_spanning_chunk_boundary() {
        val src = "let identifierLong: number = 1;"
        val factory = LexerFactory()
        val tz = factory.tokenizer(
            Version.V0,
            StringReader(src),
            LexerFactory.FeedOptions(
                maxWindowCapacity = 16,
                chunkSize = 3,
                keepTail = 2,
            ),
        )
        val r = TokenCollector.collectAll(tz)
        assertTrue(r is Success)
        val toks = (r as Success).value.map { it.toString() }
        assertTrue(toks.contains("ID(identifierLong)"))
        assertTrue(toks.contains("TYPE(NUMBER)"))
    }

    @Test
    fun rollingSlice_subSequence_materialized_returns_empty_if_head_evicted() {
        val feed = ReaderChunkFeed(StringReader("abcdef"), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1)
        val rs = feed.rollingSlice(0)

        // Empujamos hasta 5: con cap=4 esto expulsa 0..1 de la ventana
        assertTrue(feed.ensureAvailable(5))

        // subSequence(2,4) apunta a [2,3] del texto original.
        // Como 2 ya fue expulsado ANTES de crear el sub, con tu fixedSlice actual
        // el cálculo da disponible=0 ⇒ sub.toString() == "" (no lanza).
        val sub = rs.subSequence(2, 4)
        assertEquals("", sub.toString())

        // Si querés seguir testeando que una vista viva puede romperse,
        // testéalo con el RollingSlice “vivo” directamente:
        assertFailsWith<IndexOutOfBoundsException> { rs.get(0) } // 0 fue desalojado
    }

    @Test
    fun fixedSlice_subSequence_bounds_and_partialEOF() {
        val feed = ReaderChunkFeed(StringReader("hello"), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1)
        val fs = feed.fixedSlice(1, 5) // pide 1..5, pero solo hay 1..4 -> length=4 ("ello")
        assertEquals(4, fs.length)
        assertEquals("ello", fs.toString())
        val sub = fs.subSequence(1, 3) // "ll"
        assertEquals("ll", sub.toString())
    }
}
