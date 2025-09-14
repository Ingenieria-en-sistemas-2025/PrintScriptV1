import org.junit.jupiter.api.Assertions.assertEquals
import org.printscript.lexer.Scanner
import org.printscript.lexer.memory.ReaderChunkFeed
import java.io.StringReader
import kotlin.test.Test

class ScannerIntegrationTest {

    @Test
    fun advance_updatesLineAndColumn_overNewlines() {
        val text = "a\nb\ncd"
        val scan = Scanner(ReaderChunkFeed(StringReader(text), maxWindowCapacity = 4, chunkSize = 2, keepTail = 1))
        // pos inicial (1,1)
        assertEquals(1, scan.position().line)
        assertEquals(1, scan.position().column)

        val s1 = scan.advance(1) // 'a'
        assertEquals(1, s1.position().line)
        assertEquals(2, s1.position().column)

        val s2 = s1.advance(1) // '\n'
        assertEquals(2, s2.position().line)
        assertEquals(1, s2.position().column)

        val s3 = s2.advance(1) // 'b'
        assertEquals(2, s3.position().line)
        assertEquals(2, s3.position().column)
    }

    @Test
    fun slice_crossesChunks_andMaterializes() {
        val text = "abcdef"
        val scan = Scanner(ReaderChunkFeed(StringReader(text), maxWindowCapacity = 9, chunkSize = 2, keepTail = 1))
        val sl = scan.slice(5) // pide 0..4
        assertEquals("abcde", sl.toString())
    }
}
