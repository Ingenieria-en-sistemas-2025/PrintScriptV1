import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.runner.helpers.AnalyzerConfigLoaderFromStream
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AnalyzerConfigLoaderFromStreamTest {

    @Test
    fun testFromStreamReturnsDefaultConfigWhenStreamIsNull() {
        val result = AnalyzerConfigLoaderFromStream.fromStream(null)
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
    }

    @Test
    fun testFromStreamReturnsDefaultConfigWhenStreamIsEmpty() {
        val emptyStream = ByteArrayInputStream(ByteArray(0))
        val result = AnalyzerConfigLoaderFromStream.fromStream(emptyStream)
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
    }

    @Test
    fun testFromStreamCallsErrorHandlerWhenConfigIsInvalid() {
        val errorMessages = mutableListOf<String>()
        val onError: (String) -> Unit = { errorMessages.add(it) }

        val invalidConfig = "invalid json content"
        val stream = ByteArrayInputStream(invalidConfig.toByteArray())

        val result = AnalyzerConfigLoaderFromStream.fromStream(stream, onError)
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
        assertTrue(errorMessages.isNotEmpty())
        assertTrue(errorMessages.any { it.startsWith("config:") })
    }
}
