import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.factories.GlobalFormatterFactory
import org.printscript.token.TestUtils
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import java.io.StringWriter

class FormatterTestV11 {

    @Test
    fun testNestedIfStatement() {
        val formatter = GlobalFormatterFactory.forVersion(Version.V1)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("x").op().minus().number("0").sep().rparen()
                .sep().lbrace()
                .kw().ifkey().sep().lparen().identifier("x").op().minus().number("10").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("big").sep().rparen().sep().semicolon()
                .sep().rbrace()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        val expected = "if (x - 0) {\n  if (x - 10) {\n    println(\"big\");\n  }\n}"
        assertEquals(expected, output.toString())
    }

    @Test
    fun testIfKeywordSpacing() {
        val formatter = GlobalFormatterFactory.forVersion(Version.V1)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("true").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (condition) {\n  println(\"true\");\n}", output.toString())
    }

    @Test
    fun testReadInputWithConcatenation() {
        val formatter = GlobalFormatterFactory.forVersion(Version.V1)!!
        val tokenStream = TestUtils.tokens {
            kw().println().sep().lparen()
                .string("Hello ").op().plus()
                .kw().readInput().sep().lparen().string("Enter name: ").sep().rparen()
                .sep().rparen().sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("println(\"Hello \" + readInput(\"Enter name: \"));", output.toString())
    }

    @Test
    fun testComplexConditionInIf() {
        val formatter = GlobalFormatterFactory.forVersion(Version.V1)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen()
                .identifier("x").op().plus().identifier("y").op().minus().number("10")
                .sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("sum is big").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (x + y - 10) {\n  println(\"sum is big\");\n}", output.toString())
    }

    @Test
    fun testIndentationWithFourSpaces() {
        val config = FormatterConfig(indentSpaces = 4)
        val formatter = GlobalFormatterFactory.forVersion(Version.V1, config)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("x").op().minus().number("0").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("positive").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (x - 0) {\n    println(\"positive\");\n}", output.toString())
    }

    @Test
    fun testIfBraceBelowLine() {
        val config = FormatterConfig(ifBraceBelowLine = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V1, config)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("below").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (condition)\n{\n  println(\"below\");\n}", output.toString())
    }

    @Test
    fun testIfBraceSameLine() {
        val config = FormatterConfig(ifBraceSameLine = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V1, config)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("same line").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (condition) {\n  println(\"same line\");\n}", output.toString())
    }

    @Test
    fun testIfBraceBelowLineTakesPrecedenceOverSameLine() {
        val config = FormatterConfig(ifBraceBelowLine = true, ifBraceSameLine = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V1, config)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("precedence").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (condition)\n{\n  println(\"precedence\");\n}", output.toString())
    }

    @Test
    fun testNestedIfWithBraceBelowLine() {
        val config = FormatterConfig(ifBraceBelowLine = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V1, config)!!
        val tokenStream = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("x").op().minus().number("0").sep().rparen()
                .sep().lbrace()
                .kw().ifkey().sep().lparen().identifier("y").op().minus().number("5").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("nested").sep().rparen().sep().semicolon()
                .sep().rbrace()
                .sep().rbrace()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("if (x - 0)\n{\n  if (y - 5)\n  {\n    println(\"nested\");\n  }\n}", output.toString())
    }
}
