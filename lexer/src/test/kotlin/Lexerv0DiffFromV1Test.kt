import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Lexerv0DiffFromV1Test {
    private fun createTokenizer(src: String): Tokenizer {
        val cfg = PrintScriptv0MapConfig()
        return Tokenizer(src, cfg.rules(), cfg.triviaRules())
    }

    private fun lexAllToStrings(src: String): List<String> {
        val tokenizer = createTokenizer(src)
        return when (val r = tokenizer.tokenize()) {
            is Success -> r.value.map { it.toString() } // incluye EOF
            is Failure -> fail("Lexing failure: ${r.error.message} @ ${r.error.span}")
        }
    }

    private fun lexError(src: String): LabeledError {
        val tz = createTokenizer(src)
        return when (val r = tz.tokenize()) {
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
