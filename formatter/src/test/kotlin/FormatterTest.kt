import config.FormatterConfig
import dsl.TokenBuilder
import dsl.kw
import dsl.op
import dsl.sep
import dsl.ty
import factories.GlobalFormatterFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatterTest {

    private fun format(config: FormatterConfig, init: TokenBuilder.() -> TokenBuilder): String {
        val formatter: Formatter = GlobalFormatterFactory.forVersion("1.0", config)!!
        val stream: TokenStream = TestUtils.tokens(init)
        return TestUtils.assertSuccess(formatter.format(stream))
    }

    @Test
    fun testSpaceBeforeColonInDeclFalse() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = true,
            blankLinesBeforePrintln = 0,
        )
        val out = format(config) {
            kw().let().identifier("name").sep().colon().ty().stringType().op().assign().string("\"Milagros\"").sep().semicolon()
        }
        assertEquals("let name: string = \"Milagros\";\n", out)
    }

    @Test
    fun testSpaceAroundAssignmentFalse() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = false,
            blankLinesBeforePrintln = 0,
        )
        val out = format(config) {
            identifier("a").op().assign().number("5").sep().semicolon()
        }
        assertEquals("a=5;\n", out)
    }

    @Test
    fun testSpaceAroundBinary() {
        val config = FormatterConfig()
        val out = format(config) {
            identifier("a").op().plus().identifier("b").op().multiply().identifier("c").sep().semicolon()
        }
        assertEquals("a + b * c;\n", out)
    }

    @Test
    fun testSemiColonForcesBlankLine() {
        val config = FormatterConfig()
        val out = format(config) {
            identifier("x").op().assign().number("1").sep().semicolon()
                .identifier("y").op().assign().number("2").sep().semicolon()
        }
        assertEquals("x = 1;\ny = 2;\n", out)
    }

    @Test
    fun testPrintlnInsertsNlines() {
        val config = FormatterConfig(blankLinesBeforePrintln = 1)
        val out = format(config) {
            kw().let().identifier("a").sep().colon().ty().numberType().op().assign().number("1").sep().semicolon()
                .kw().println().sep().lparen().identifier("a").sep().rparen().sep().semicolon()
        }
        val expected = buildString {
            append("let a: number = 1;\n")
            append("\n")
            append("println(a);\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testDefaultSpacing() {
        val config = FormatterConfig()
        val out = format(config) {
            kw().let().identifier("x").sep().semicolon()
        }
        assertEquals("let x;\n", out)
    }
}
