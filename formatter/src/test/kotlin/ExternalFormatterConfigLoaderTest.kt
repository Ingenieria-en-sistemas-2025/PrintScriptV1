import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.printscript.formatter.config.ExternalFormatterConfigLoader

class ExternalFormatterConfigLoaderTest {

    @Test
    fun testReturnsDefaultConfig() {
        val result = ExternalFormatterConfigLoader.load(null)

        assertEquals(ExternalFormatterConfigLoader.indentDefault, result.indentSpaces)
        assertNull(result.spaceAroundAssignment)
        assertNull(result.spaceBeforeColonInDecl)
        assertNull(result.spaceAfterColonInDecl)
        assertNull(result.blankLinesAfterPrintln)
        assertNull(result.mandatorySingleSpaceSeparation)
        assertNull(result.ifBraceBelowLine)
        assertNull(result.ifBraceSameLine)
    }

    @Test
    fun testReturnsDefaultConfigWhenNullConfig() {
        val result = ExternalFormatterConfigLoader.load(byteArrayOf())
        assertEquals(ExternalFormatterConfigLoader.indentDefault, result.indentSpaces)
        assertNull(result.spaceAroundAssignment)
    }

    @Test
    fun `should return default config when invalid JSON`() {
        val invalidJson = "invalid json".toByteArray()
        val result = ExternalFormatterConfigLoader.load(invalidJson)

        assertEquals(ExternalFormatterConfigLoader.indentDefault, result.indentSpaces)
    }

    @Test
    fun testLoadsBasicConfig() {
        val json = """
            {
                "indentSpaces": 4,
                "spaceAroundAssignment": true,
                "spaceBeforeColonInDecl": false,
                "spaceAfterColonInDecl": true,
                "blankLinesAfterPrintln": 2,
                "mandatorySingleSpaceSeparation": true,
                "ifBraceBelowLine": true,
                "ifBraceSameLine": false
            }
        """.trimIndent().toByteArray()

        val result = ExternalFormatterConfigLoader.load(json)

        assertEquals(4, result.indentSpaces)
        assertEquals(true, result.spaceAroundAssignment)
        assertEquals(false, result.spaceBeforeColonInDecl)
        assertEquals(true, result.spaceAfterColonInDecl)
        assertEquals(2, result.blankLinesAfterPrintln)
        assertEquals(true, result.mandatorySingleSpaceSeparation)
        assertEquals(true, result.ifBraceBelowLine)
        assertEquals(false, result.ifBraceSameLine)
    }

    @Test
    fun testHandleAliasEnforcespacingaroundequals() {
        val json = """
            {
                "enforce-spacing-around-equals": true
            }
        """.trimIndent().toByteArray()

        val result = ExternalFormatterConfigLoader.load(json)

        assertEquals(true, result.spaceAroundAssignment)
    }

    @Test
    fun testAliasEnforcenospacingaroundequals() {
        val json = """
            {
                "enforce-no-spacing-around-equals": true
            }
        """.trimIndent().toByteArray()

        val result = ExternalFormatterConfigLoader.load(json)

        assertEquals(false, result.spaceAroundAssignment)
    }

    @Test
    fun testAliasEnforcenospacingaroundequalsWhenFalse() {
        val json = """
            {
                "enforce-no-spacing-around-equals": false
            }
        """.trimIndent().toByteArray()

        val result = ExternalFormatterConfigLoader.load(json)

        assertEquals(true, result.spaceAroundAssignment)
    }

    @Test
    fun testHandlesEnforcespacingaroundequalsTakesPresedenceOverEnforcenospacingaroundequals() {
        val json = """
            {
                "enforce-spacing-around-equals": false,
                "enforce-no-spacing-around-equals": true
            }
        """.trimIndent().toByteArray()

        val result = ExternalFormatterConfigLoader.load(json)

        assertEquals(false, result.spaceAroundAssignment)
    }

    @Test
    fun testShouldHandleColonAlias() {
        val json = """
            {
                "enforce-spacing-after-colon-in-declaration": true,
                "enforce-spacing-before-colon-in-declaration": false
            }
        """.trimIndent().toByteArray()

        val result = ExternalFormatterConfigLoader.load(json)

        assertEquals(true, result.spaceAfterColonInDecl)
        assertEquals(false, result.spaceBeforeColonInDecl)
    }
}
