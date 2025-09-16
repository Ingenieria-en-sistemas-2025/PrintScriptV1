import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.runner.helpers.AnalyzerConfigLoaderFromPath
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AnalyzerConfigLoaderFromPathTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testFromPathReturnsDefaultConfigWhenPathIsNull() {
        val result = AnalyzerConfigLoaderFromPath.fromPath(null)
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
    }

    @Test
    fun testFromPathReturnsDefaultConfigWhenPathIsBlank() {
        val result = AnalyzerConfigLoaderFromPath.fromPath("   ")
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
    }

    @Test
    fun testFromPathReturnsDefaultConfigWhenFileDoesNotExist() {
        val nonExistentPath = tempDir.resolve("nonexistent.json").toString()
        val result = AnalyzerConfigLoaderFromPath.fromPath(nonExistentPath)
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
    }

    @Test
    fun testFromPathLoadsValidJsonConfig() {
        val configFile = tempDir.resolve("config.json")
        Files.write(configFile, """{"identifiers": {"checkDeclaration": true}}""".toByteArray())

        val result = AnalyzerConfigLoaderFromPath.fromPath(configFile.toString())
        assertNotNull(result)
    }

    @Test
    fun testFromPathFallsBackToYamlWhenJsonFails() {
        val configFile = tempDir.resolve("config.yaml")
        Files.write(configFile, """identifiers:\n  checkDeclaration: true""".toByteArray())

        val result = AnalyzerConfigLoaderFromPath.fromPath(configFile.toString())
        assertNotNull(result)
    }

    @Test
    fun testFromPathReturnsDefaultWhenBothJsonAndYamlFail() {
        val configFile = tempDir.resolve("config.txt")
        Files.write(configFile, """invalid config content""".toByteArray())

        val result = AnalyzerConfigLoaderFromPath.fromPath(configFile.toString())
        assertNotNull(result)
        assertEquals(AnalyzerConfig(), result)
    }
}
