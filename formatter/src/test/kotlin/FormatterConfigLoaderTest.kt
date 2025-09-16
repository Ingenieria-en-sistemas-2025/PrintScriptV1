import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.formatter.config.ConfigLoader
import java.nio.file.Files
import java.nio.file.Path

class FormatterConfigLoaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testLoadDefaultConfigWhenPathIsNull() {
        val config = ConfigLoader.load(null)

        assertNull(config.spaceBeforeColonInDecl)
        assertNull(config.spaceAfterColonInDecl)
        assertNull(config.spaceAroundAssignment)
        assertNull(config.blankLinesAfterPrintln)
        assertEquals(2, config.indentSpaces)
        assertNull(config.mandatorySingleSpaceSeparation)
        assertNull(config.ifBraceBelowLine)
        assertNull(config.ifBraceSameLine)
    }

    @Test
    fun testLoadJsonConfig() {
        val jsonContent = """
            {
                "spaceBeforeColonInDecl": true,
                "spaceAfterColonInDecl": false,
                "spaceAroundAssignment": true,
                "blankLinesAfterPrintln": 2,
                "indentSpaces": 4,
                "mandatorySingleSpaceSeparation": true,
                "ifBraceBelowLine": false,
                "ifBraceSameLine": true
            }
        """.trimIndent()

        val configFile = tempDir.resolve("config.json")
        Files.write(configFile, jsonContent.toByteArray())

        val config = ConfigLoader.load(configFile)

        assertTrue(config.spaceBeforeColonInDecl!!)
        assertFalse(config.spaceAfterColonInDecl!!)
        assertTrue(config.spaceAroundAssignment!!)
        assertEquals(2, config.blankLinesAfterPrintln)
        assertEquals(4, config.indentSpaces)
        assertTrue(config.mandatorySingleSpaceSeparation!!)
        assertFalse(config.ifBraceBelowLine!!)
        assertTrue(config.ifBraceSameLine!!)
    }

    @Test
    fun testLoadYamlConfig() {
        val yamlContent = """
            spaceBeforeColonInDecl: false
            spaceAfterColonInDecl: true
            spaceAroundAssignment: false
            blankLinesAfterPrintln: 1
            indentSpaces: 2
            mandatorySingleSpaceSeparation: false
            ifBraceBelowLine: true
            ifBraceSameLine: false
        """.trimIndent()

        val configFile = tempDir.resolve("config.yaml")
        Files.write(configFile, yamlContent.toByteArray())

        val config = ConfigLoader.load(configFile)

        assertFalse(config.spaceBeforeColonInDecl!!)
        assertTrue(config.spaceAfterColonInDecl!!)
        assertFalse(config.spaceAroundAssignment!!)
        assertEquals(1, config.blankLinesAfterPrintln)
        assertEquals(2, config.indentSpaces)
        assertFalse(config.mandatorySingleSpaceSeparation!!)
        assertTrue(config.ifBraceBelowLine!!)
        assertFalse(config.ifBraceSameLine!!)
    }

    @Test
    fun testLoadYmlConfig() {
        val yamlContent = """
            spaceBeforeColonInDecl: true
            spaceAfterColonInDecl: true
            indentSpaces: 8
        """.trimIndent()

        val configFile = tempDir.resolve("config.yml")
        Files.write(configFile, yamlContent.toByteArray())

        val config = ConfigLoader.load(configFile)

        assertTrue(config.spaceBeforeColonInDecl!!)
        assertTrue(config.spaceAfterColonInDecl!!)
        assertEquals(8, config.indentSpaces)
        assertNull(config.spaceAroundAssignment)
        assertNull(config.blankLinesAfterPrintln)
        assertNull(config.mandatorySingleSpaceSeparation)
        assertNull(config.ifBraceBelowLine)
        assertNull(config.ifBraceSameLine)
    }

    @Test
    fun testLoadPartialJsonConfig() {
        val jsonContent = """
            {
                "spaceAroundAssignment": false,
                "indentSpaces": 6
            }
        """.trimIndent()

        val configFile = tempDir.resolve("partial.json")
        Files.write(configFile, jsonContent.toByteArray())

        val config = ConfigLoader.load(configFile)

        assertFalse(config.spaceAroundAssignment!!)
        assertEquals(6, config.indentSpaces)
        assertNull(config.spaceBeforeColonInDecl)
        assertNull(config.spaceAfterColonInDecl)
        assertNull(config.blankLinesAfterPrintln)
        assertNull(config.mandatorySingleSpaceSeparation)
        assertNull(config.ifBraceBelowLine)
        assertNull(config.ifBraceSameLine)
    }
}
