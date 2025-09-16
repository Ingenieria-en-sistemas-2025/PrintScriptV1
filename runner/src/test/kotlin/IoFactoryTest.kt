import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.printscript.runner.helpers.IoFactory
import java.io.StringReader

class IoFactoryTest {

    @Test
    fun testFromReaderCreatesProgramIoWithReaderOnly() {
        val reader = StringReader("test content")
        val result = IoFactory.fromReader(reader)

        assertNotNull(result)
        assertEquals(reader, result.reader)
        assertNull(result.inputProviderOverride)
    }

    @Test
    fun testFromReaderWithInputCreatesProgramIoWithReaderAndInputProvider() {
        val reader = StringReader("test content")
        val inputFunction = { prompt: String -> "user input for $prompt" }

        val result = IoFactory.fromReaderWithInput(reader, inputFunction)

        assertNotNull(result)
        assertEquals(reader, result.reader)
        assertNotNull(result.inputProviderOverride)
        assertEquals("user input for test prompt", result.inputProviderOverride!!.read("test prompt"))
    }

    @Test
    fun testFromReaderWithInputHandlesNullInputFunction() {
        val reader = StringReader("test content")

        val result = IoFactory.fromReaderWithInput(reader, null)

        assertNotNull(result)
        assertEquals(reader, result.reader)
        assertNull(result.inputProviderOverride)
    }
}
