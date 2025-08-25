import config.FormatterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class FormatterTest {

    private fun format(config: FormatterConfig, tokens: List<Token>): String =
        Formatter(config).format(tokens)

    private fun dummySpan(): Span = Span(Position(1, 1), Position(1, 1))

    @Test
    fun testSpaceBeforeColonInDeclFalse() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = true,
            blankLinesBeforePrintln = 0)
        val tokens = listOf(
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("name", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.STRING, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            StringLiteralToken("\"Milagros\"", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()))
        val out = format(config, tokens)
        // let name: string = "Milagros";\n
        assertEquals("let name: string = \"Milagros\";\n", out)
    }

    @Test
    fun testSpaceAroundAssignmentFalse() {
        val config = FormatterConfig(
            spaceBeforeColonInDecl = false,
            spaceAfterColonInDecl = true,
            spaceAroundAssignment = false,
            blankLinesBeforePrintln = 0
        )
        val tokens = listOf(
            IdentifierToken("a", dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("5", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()))
        val out = format(config, tokens)
        assertEquals("a=5;\n", out)
    }

    @Test
    fun testSpaceAroundBinary() {
        val config = FormatterConfig()
        val tokens = listOf(
            IdentifierToken("a", dummySpan()),
            OperatorToken(Operator.PLUS, dummySpan()),
            IdentifierToken("b", dummySpan()),
            OperatorToken(Operator.MULTIPLY, dummySpan()),
            IdentifierToken("c", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()))
        val out = format(config, tokens)
        assertEquals("a + b * c;\n", out)
    }

    @Test
    fun testSemiColonForcesBlankLine() {
        val config = FormatterConfig()
        val tokens = listOf(
            IdentifierToken("x", dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("1", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            IdentifierToken("y", dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("2", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()))
        val out = format(config, tokens)
        assertEquals("x = 1;\ny = 2;\n", out)
    }

    @Test
    fun testPrintlnInsertsNlines() {
        val config = FormatterConfig(blankLinesBeforePrintln = 1) // 1 linea en blanco
        val tokens = listOf(
            // let a: number = 1;
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("a", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.NUMBER, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("1", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            // println(a);
            KeywordToken(Keyword.PRINTLN, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("a", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()))
        val out = format(config, tokens)
        val expected = buildString {
            append("let a: number = 1;\n")
            append("\n") // blank line extra
            append("println(a);\n") }
        assertEquals(expected, out)
    }

    @Test
    fun testDefaultSpacing() {
        val config = FormatterConfig()
        val tokens = listOf(
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("x", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan())
        )
        val out = format(config, tokens)
        assertEquals("let x;\n", out) // espacio entre let y x
    }
}
