import config.FormatterConfig
import dsl.TokenBuilder
import dsl.kw
import dsl.op
import dsl.sep
import dsl.ty
import factories.FormatterFactoryV11
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class FormatterV11Test {

    private fun unwrapOrFail(res: Result<String, LabeledError>): String =
        res.fold(
            onSuccess = { it },
            onFailure = { err -> fail("Formatting failed at ${err.humanReadable()}") },
        )

    private fun format(config: FormatterConfig, tokenStream: TokenStream): String {
        val formatter: Formatter = FormatterFactoryV11.create(config)
        return unwrapOrFail(formatter.format(tokenStream))
    }

    private fun tokens(block: TokenBuilder.() -> TokenBuilder): TokenStream {
        return TokenBuilder().block().build()
    }

    @Test
    fun testBasicIfBlockIndentation() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = true,
            blankLinesBeforePrintln = 0,
            indentSpaces = 4,
        )

        val tokenStream = tokens {
            kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                .sep().lbrace()
                .identifier("x").op().assign().number("5").sep().semicolon()
                .sep().rbrace()
        }

        val out = format(config, tokenStream)
        val expected = buildString {
            append("if (condition) {\n")
            append("    x = 5;\n")
            append("}\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testIfElseBlockIndentation() {
        val config = FormatterConfig(
            indentSpaces = 2,
        )

        val tokenStream = tokens {
            kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("\"true\"").sep().rparen().sep().semicolon()
                .sep().rbrace()
                .kw().elsekey().sep().lbrace()
                .kw().println().sep().lparen().string("\"false\"").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val out = format(config, tokenStream)
        val expected = buildString {
            append("if (condition) {\n")
            append("  println(\"true\");\n")
            append("} else {\n")
            append("  println(\"false\");\n")
            append("}\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testMultipleStatementsInIfBlock() {
        val config = FormatterConfig(
            indentSpaces = 4,
            blankLinesBeforePrintln = 1,
        )

        val tokenStream = tokens {
            kw().ifkey().sep().lparen().identifier("x").sep().rparen()
                .sep().lbrace()
                .kw().let().identifier("y").sep().colon().ty().numberType().op().assign().number("10").sep().semicolon()
                .identifier("x").op().assign().identifier("x").op().plus().identifier("y").sep().semicolon()
                .kw().println().sep().lparen().identifier("x").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val out = format(config, tokenStream)
        val expected = buildString {
            append("if (x) {\n")
            append("    let y: number = 10;\n")
            append("    x = x + y;\n")
            append("\n")
            append("    println(x);\n")
            append("}\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testConstDeclarationFormatting() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = true,
        )

        val tokenStream = tokens {
            kw().const().identifier("PI").sep().colon().ty().numberType().op().assign().number("3.14").sep().semicolon()
        }

        val out = format(config, tokenStream)
        assertEquals("const PI: number = 3.14;\n", out)
    }

    @Test
    fun testNestedIfWithCustomIndentation() {
        val config = FormatterConfig(indentSpaces = 8)

        val tokenStream = tokens {
            kw().ifkey().sep().lparen().identifier("outer").sep().rparen()
                .sep().lbrace()
                .kw().ifkey().sep().lparen().identifier("inner").sep().rparen()
                .sep().lbrace()
                .identifier("x").op().assign().number("1").sep().semicolon()
                .sep().rbrace()
                .sep().rbrace()
        }

        val out = format(config, tokenStream)
        val expected = buildString {
            append("if (outer) {\n")
            append("        if (inner) {\n")
            append("                x = 1;\n")
            append("        }\n")
            append("}\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testReadInputTopLevel() {
        val config = FormatterConfig(indentSpaces = 4)

        val tokenStream = tokens {
            kw().readInput().sep().lparen().string("\"Enter: \"").sep().rparen().sep().semicolon()
        }

        val out = format(config, tokenStream)
        assertEquals("readInput(\"Enter: \");\n", out)
    }

    @Test
    fun testReadEnvAssignmentFormatting() {
        val config = FormatterConfig(
            spaceAroundAssignment = true,
            indentSpaces = 4,
        )

        val tokenStream = tokens {
            identifier("user").op().assign().kw().readEnv().sep().lparen().string("\"USER\"").sep().rparen().sep().semicolon()
        }

        val out = format(config, tokenStream)
        assertEquals("user = readEnv(\"USER\");\n", out)
    }

    @Test
    fun testNestedReadInputInPrintln() {
        val config = FormatterConfig()

        val tokenStream = tokens {
            kw().println().sep().lparen().kw().readInput().sep().lparen().string("\"msg\"").sep().rparen().sep().rparen().sep().semicolon()
        }

        val out = format(config, tokenStream)
        assertEquals("println(readInput(\"msg\"));\n", out)
    }

    @Test
    fun testReadEnvInsideIfBlockIndentation() {
        val config = FormatterConfig(indentSpaces = 2)

        val tokenStream = tokens {
            kw().ifkey().sep().lparen().identifier("cond").sep().rparen()
                .sep().lbrace()
                .kw().readEnv().sep().lparen().string("\"PATH\"").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val out = format(config, tokenStream)
        val expected = buildString {
            append("if (cond) {\n")
            append("  readEnv(\"PATH\");\n")
            append("}\n")
        }
        assertEquals(expected, out)
    }
}
