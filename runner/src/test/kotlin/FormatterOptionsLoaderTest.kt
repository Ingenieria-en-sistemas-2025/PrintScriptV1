import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.formatter.config.FormatterConfig
import org.printscript.runner.helpers.FormatterOptionsLoader
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path

class FormatterOptionsLoaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testFromPathReturnsDefaultWhenPathIsNull() {
        val result = FormatterOptionsLoader.fromPath(null)
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromPathReturnsDefaultWhenPathIsBlank() {
        val result = FormatterOptionsLoader.fromPath("   ")
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromPathReturnsDefaultWhenFileDoesNotExist() {
        val nonExistentPath = tempDir.resolve("nonexistent.json").toString()
        val result = FormatterOptionsLoader.fromPath(nonExistentPath)
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromPathLoadsValidConfigFile() {
        val configFile = tempDir.resolve("formatter.json")
        Files.write(configFile, """{"indentSize": 4}""".toByteArray())

        val result = FormatterOptionsLoader.fromPath(configFile.toString())
        assertNotNull(result)
    }

    @Test
    fun testFromPathReturnsDefaultWhenFileIsEmpty() {
        val configFile = tempDir.resolve("empty.json")
        Files.write(configFile, ByteArray(0))

        val result = FormatterOptionsLoader.fromPath(configFile.toString())
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromStreamReturnsDefaultWhenStreamIsNull() {
        val result = FormatterOptionsLoader.fromStream(null)
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromStreamReturnsDefaultWhenStreamIsEmpty() {
        val emptyStream = ByteArrayInputStream(ByteArray(0))
        val result = FormatterOptionsLoader.fromStream(emptyStream)
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromStreamLoadsValidConfig() {
        val configContent = """{"indentSize": 2}"""
        val stream = ByteArrayInputStream(configContent.toByteArray())

        val result = FormatterOptionsLoader.fromStream(stream)
        assertNotNull(result)
    }

    @Test
    fun testFromBytesReturnsDefaultWhenBytesAreNull() {
        val result = FormatterOptionsLoader.fromBytes(null)
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromBytesReturnsDefaultWhenBytesAreEmpty() {
        val result = FormatterOptionsLoader.fromBytes(ByteArray(0))
        assertNotNull(result)
        assertEquals(FormatterConfig(), result)
    }

    @Test
    fun testFromBytesLoadsValidConfig() {
        val configContent = """{"indentSize": 3}"""
        val bytes = configContent.toByteArray()

        val result = FormatterOptionsLoader.fromBytes(bytes)
        assertNotNull(result)
    }
}
