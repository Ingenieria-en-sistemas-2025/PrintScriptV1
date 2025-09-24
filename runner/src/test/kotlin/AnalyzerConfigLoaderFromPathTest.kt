import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.runner.helpers.AnalyzerConfigResolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertNotNull

class AnalyzerConfigLoaderFromPathTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testFromPathLoadsValidJsonConfig() {
        val configFile = tempDir.resolve("config.json")
        Files.write(configFile, """{"identifiers": {"checkDeclaration": true}}""".toByteArray())

        val result = AnalyzerConfigResolver.fromPathStrict(configFile.toString())
        assertNotNull(result)
    }

    @Test
    fun testFromPathFallsBackToYamlWhenJsonFails() {
        val configFile = tempDir.resolve("config.yaml")
        Files.write(configFile, """identifiers:\n  checkDeclaration: true""".toByteArray())

        val result = AnalyzerConfigResolver.fromPathStrict(configFile.toString())
        assertNotNull(result)
    }
}
