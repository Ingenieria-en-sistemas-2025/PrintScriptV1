import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.common.Version
import org.printscript.formatter.Formatter
import org.printscript.formatter.config.ConfigLoader
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions
import org.printscript.formatter.factories.GlobalFormatterFactory
import org.printscript.token.TestUtils
import org.printscript.token.TokenStream
import org.printscript.token.dsl.TokenBuilder
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import java.nio.file.Files
import java.nio.file.Path

class FormatterConfigFileTest {

    @TempDir // crea directorio temporal para escribir archivos para test
    lateinit var tmp: Path

    private fun write(path: Path, content: String): Path {
        Files.writeString(path, content.trimIndent())
        return path
    }

    private fun formatWith(config: FormatterConfig, init: TokenBuilder.() -> TokenBuilder): String {
        val formatter: Formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val stream: TokenStream = TestUtils.tokens(init)
        return TestUtils.assertSuccess(formatter.format(stream))
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

        val out = formatWith(config as FormatterConfig) {
            kw().let().identifier("name").sep().colon().ty().stringType().op().assign().string("\"Milagros\"").sep().semicolon()
                .kw().println().sep().lparen().identifier("name").sep().rparen().sep().semicolon()
        }

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

        val out = formatWith(config as FormatterConfig) {
            kw().let().identifier("a").sep().colon().ty().numberType().op().assign().number("1").sep().semicolon()
                .kw().println().sep().lparen().identifier("a").sep().rparen().sep().semicolon()
        }

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

        val out = formatWith(config as FormatterConfig) {
            kw().let().identifier("n").sep().colon().ty().numberType().op().assign().number("7").sep().semicolon()
        }

        val expected = "let n:number = 7;\n"
        assertEquals(expected, out)
    }
}
