import config.ConfigLoader
import config.FormatterOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
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

    private fun streamOf(tokens: List<Token>): TokenStream =
        ListTokenStream.of(tokens)

    private fun unwrap(res: Result<String, LabeledError>): String =
        res.fold(
            onSuccess = { it },
            onFailure = { err -> fail("Formatting failed at ${err.humanReadable()}") },
        )

    private fun formatWith(config: FormatterOptions, tokens: List<Token>): String {
        val formatter: Formatter = FirstFormatter(config)
        val ts = streamOf(tokens)
        return unwrap(formatter.format(ts))
    }

    @Test
    fun testFormatterYaml() {
        val yaml = """
            spaceBeforeColonInDecl: false
            spaceAfterColonInDecl:  true
            spaceAroundAssignment:  true
            blankLinesBeforePrintln: 1
        """
        val configPath = write(tmp.resolve("formatter.yaml"), yaml)
        val config: FormatterOptions = ConfigLoader.load(configPath)

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
        val config: FormatterOptions = ConfigLoader.load(configPath)

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
            append("\n\n")
            append("println(a);\n")
        }
        assertEquals(expected, out)
    }

    @Test
    fun testFormatterNoExtensionAlsoFallsBackToJson() {
        val jsonLike = """
        {
          "spaceBeforeColonInDecl": false,
          "spaceAfterColonInDecl":  false,
          "spaceAroundAssignment":  true,
          "blankLinesBeforePrintln": 0
        }
        """
        val configPath = write(tmp.resolve("formatter"), jsonLike)
        val config: FormatterOptions = ConfigLoader.load(configPath)

        val tokens = listOf(
            KeywordToken(Keyword.LET, dummySpan()),
            IdentifierToken("n", dummySpan()),
            SeparatorToken(Separator.COLON, dummySpan()),
            TypeToken(Type.NUMBER, dummySpan()),
            OperatorToken(Operator.ASSIGN, dummySpan()),
            NumberLiteralToken("7", dummySpan()),
            SeparatorToken(Separator.SEMICOLON, dummySpan()),
            EofToken(dummySpan()),
        )

        val out = formatWith(config, tokens)
        val expected = "let n:number = 7;\n"
        assertEquals(expected, out)
    }
}
