import config.ConfigLoader
import config.FormatterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class FormatterConfigFileTest {

    @TempDir
    lateinit var tmp: Path

    private fun dummySpan(): Span = Span(Position(1, 1), Position(1, 1))

    private fun write(path: Path, content: String): Path {
        Files.writeString(path, content.trimIndent())
        return path
    }

    private fun formatWith(config: FormatterConfig, tokens: List<Token>): String =
        Formatter(config).format(tokens)

    @Test
    fun testFormatterYaml() {
        val yaml = """
            spaceBeforeColonInDecl: false
            spaceAfterColonInDecl:  true
            spaceAroundAssignment:  true
            blankLinesBeforePrintln: 1
        """
        val configPath = write(tmp.resolve("formatter.yaml"), yaml)
        val config: FormatterConfig = ConfigLoader.load(configPath)

        val tokens = listOf(
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("name", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.STRING, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            StringLiteralToken("\"Milagros\"", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            KeywordToken(Keyword.PRINTLN, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("name", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()),
        )
        val out = formatWith(config, tokens)
        val expected = buildString {
            append("let name: string = \"Milagros\";\n")
            append("\n")
            append("println(name);\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testFormatterJson() {
        val json = """
        {
          "spaceBeforeColonInDecl": true,
          "spaceAfterColonInDecl":  false,
          "spaceAroundAssignment":  false,
          "blankLinesBeforePrintln": 2
        }
        """
        val configPath = write(tmp.resolve("formatter.json"), json)
        val config: FormatterConfig = ConfigLoader.load(configPath)

        val tokens = listOf(
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("a", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.NUMBER, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("1", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            KeywordToken(Keyword.PRINTLN, dummySpan()),
            SeparatorToken(Separator.LPAREN, dummySpan()),
            IdentifierToken("a", dummySpan()),
            SeparatorToken(Separator.RPAREN, dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()),
        )
        val out = formatWith(config, tokens)
        val expected = buildString {
            append("let a :number=1;\n")
            append("\n\n") // 2 lineas en blanco
            append("println(a);\n")
        }
        assertEquals(expected, out)
    }
}
