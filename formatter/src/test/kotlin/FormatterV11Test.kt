import config.FormatterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class FormatterV11Test {

    private fun unwrapOrFail(res: Result<String, LabeledError>): String =
        res.fold(
            onSuccess = { it },
            onFailure = { err -> fail("Formatting failed at ${err.humanReadable()}") },
        )

    private fun streamOf(tokens: List<Token>): TokenStream =
        ListTokenStream.of(tokens)

    private fun format(config: FormatterConfig, tokens: List<Token>): String {
        val formatter: Formatter = EnhancedFormatter(config)
        val stream: TokenStream = streamOf(tokens)
        return unwrapOrFail(formatter.format(stream))
    }

    private fun dummySpan(): Span = Span(Position(1, 1), Position(1, 1))

    @Test
    fun testBasicIfBlockIndentation() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = true,
            blankLinesBeforePrintln = 0,
            indentSpaces = 4,
        )

        val tokens = listOf(
            KeywordToken(Keyword.IF, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("condition", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.LBRACE, dummySpan()),
            IdentifierToken("x", dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("5", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            SeparatorToken(Separator.RBRACE, dummySpan()),
            EofToken(dummySpan()),
        )

        val out = format(config, tokens)
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

        val tokens = listOf(
            KeywordToken(Keyword.IF, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("condition", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.LBRACE, dummySpan()),
            KeywordToken(Keyword.PRINTLN, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            StringLiteralToken("\"true\"", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            SeparatorToken(Separator.RBRACE, dummySpan()),
            KeywordToken(Keyword.ELSE, dummySpan()),
            SeparatorToken(Separator.LBRACE, dummySpan()),
            KeywordToken(Keyword.PRINTLN, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            StringLiteralToken("\"false\"", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            SeparatorToken(Separator.RBRACE, dummySpan()),
            EofToken(dummySpan()),
        )

        val out = format(config, tokens)
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

        val tokens = listOf(
            KeywordToken(Keyword.IF, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("x", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.LBRACE, dummySpan()),
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("y", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.NUMBER, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("10", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            IdentifierToken("x", dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            IdentifierToken("x", dummySpan()),
            OperatorToken(Operator.PLUS, dummySpan()),
            IdentifierToken("y", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            KeywordToken(Keyword.PRINTLN, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("x", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            SeparatorToken(Separator.RBRACE, dummySpan()),
            EofToken(dummySpan()),
        )

        val out = format(config, tokens)
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

        val tokens = listOf(
            KeywordToken(Keyword.CONST, dummySpan()),
            IdentifierToken("PI", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.NUMBER, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("3.14", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()),
        )

        val out = format(config, tokens)
        assertEquals("const PI: number = 3.14;\n", out)
    }

    @Test
    fun testNestedIfWithCustomIndentation() {
        val config = FormatterConfig(indentSpaces = 8)

        val tokens = listOf(
            KeywordToken(Keyword.IF, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("outer", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.LBRACE, dummySpan()),
            KeywordToken(Keyword.IF, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("inner", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.LBRACE, dummySpan()),
            IdentifierToken("x", dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("1", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            SeparatorToken(Separator.RBRACE, dummySpan()),
            SeparatorToken(Separator.RBRACE, dummySpan()),
            EofToken(dummySpan()),
        )

        val out = format(config, tokens)
        val expected = buildString {
            append("if (outer) {\n")
            append("        if (inner) {\n")
            append("                x = 1;\n")
            append("        }\n")
            append("}\n")
        }
        assertEquals(expected, out)
    }
}
