import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.PrintScriptv0MapConfig
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.triviarules.CompositeTriviaSkipper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Lexerv0DiffFromV1Test {
    private fun createTokenizer(src: String): Tokenizer {
        val cfg = PrintScriptv0MapConfig() // o v1 en el otro test
        val matcher = LongestMatchTokenMatcher(cfg.rules())
        val skipper = CompositeTriviaSkipper(cfg.triviaRules())
        return Tokenizer.of(src, matcher, skipper)
    }

    private fun lexAllToStrings(src: String): List<String> {
        val tokenizer = createTokenizer(src)
        return when (val r = TokenCollector.collectAll(tokenizer)) {
            is Success -> r.value.map { it.toString() } // incluye EOF al final
            is Failure -> fail("Lexing failure: ${r.error.message} @ ${r.error.span}")
        }
    }

    private fun lexError(src: String): LexerError {
        val tz = createTokenizer(src)
        return when (val r = TokenCollector.collectAll(tz)) {
            is Success -> fail("Se esperaba Failure, pero se obtuvo Success con ${r.value.size} tokens")
            is Failure -> r.error
        }
    }

    @Test
    fun trueFalseSonIdentificadoresEnV0() {
        val actual = lexAllToStrings("true false trueValue")
        val expected = listOf(
            "ID(true)",
            "ID(false)",
            "ID(trueValue)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun constEsIdentificadorEnV0() {
        val actual = lexAllToStrings("const x: number = 1;")
        val expected = listOf(
            "ID(const)",
            "ID(x)",
            "SEP(COLON)",
            "TYPE(NUMBER)",
            "OP(ASSIGN)",
            "NUM(1)",
            "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun readInputYReadEnvSonIdentifiersEnV0() {
        val src = """
            let name: string = readInput("Name: ");
            let debug: string = readEnv("DEBUG");
        """.trimIndent()
        val actual = lexAllToStrings(src)
        val expected = listOf(
            "KW(LET)", "ID(name)", "SEP(COLON)", "TYPE(STRING)", "OP(ASSIGN)",
            "ID(readInput)", "SEP(LPAREN)", "STR(\"Name: \")", "SEP(RPAREN)", "SEP(SEMICOLON)",

            "KW(LET)", "ID(debug)", "SEP(COLON)", "TYPE(STRING)", "OP(ASSIGN)",
            "ID(readEnv)", "SEP(LPAREN)", "STR(\"DEBUG\")", "SEP(RPAREN)", "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun ifElseYLlavesNoSoportadosEnV0_disparaUnexpectedCharPorLlave() {
        val err = lexError("""if (x) { println("ok"); }""")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }
}
