import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Lexerv1Test {

    private fun createTokenizer(src: String): Tokenizer {
        val cfg = PrintScriptv1MapConfig()
        return Tokenizer(src, cfg.rules(), cfg.triviaRules())
    }

    private fun lexAllToStrings(src: String): List<String> {
        val tokenizer = createTokenizer(src)
        return when (val r = tokenizer.tokenize()) {
            is Success -> r.value.map { it.toString() } // incluye EOF al final
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
    fun constBooleanTrueLiteral() {
        val src = """const flag: boolean = true;"""
        val actual = lexAllToStrings(src)
        val expected = listOf(
            "KW(CONST)",
            "ID(flag)",
            "SEP(COLON)",
            "TYPE(BOOLEAN)",
            "OP(ASSIGN)",
            "BOOL(true)",
            "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun ifElseConLlavesYPrintln() {
        val src = """if (flag) { println("ok"); } else { println("no"); }"""
        val actual = lexAllToStrings(src)
        val expected = listOf(
            "KW(IF)", "SEP(LPAREN)", "ID(flag)", "SEP(RPAREN)",
            "SEP(LBRACE)",
            "KW(PRINTLN)", "SEP(LPAREN)", "STR(\"ok\")", "SEP(RPAREN)", "SEP(SEMICOLON)",
            "SEP(RBRACE)",
            "KW(ELSE)",
            "SEP(LBRACE)",
            "KW(PRINTLN)", "SEP(LPAREN)", "STR(\"no\")", "SEP(RPAREN)", "SEP(SEMICOLON)",
            "SEP(RBRACE)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun readInputYReadEnvComoKeywords() {
        val src = """
            let name: string = readInput("Name: ");
            let debug: boolean = readEnv("DEBUG");
        """.trimIndent()
        val actual = lexAllToStrings(src)
        val expected = listOf(
            "KW(LET)", "ID(name)", "SEP(COLON)", "TYPE(STRING)", "OP(ASSIGN)",
            "KW(READ_INPUT)", "SEP(LPAREN)", "STR(\"Name: \")", "SEP(RPAREN)", "SEP(SEMICOLON)",

            "KW(LET)", "ID(debug)", "SEP(COLON)", "TYPE(BOOLEAN)", "OP(ASSIGN)",
            "KW(READ_ENV)", "SEP(LPAREN)", "STR(\"DEBUG\")", "SEP(RPAREN)", "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun trueFalseSonLiteralsNoIdentificadores() {
        val actual = lexAllToStrings("true false trueValue")
        val expected = listOf(
            "BOOL(true)",
            "BOOL(false)",
            "ID(trueValue)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun numerosYStringsSiguenIgual() {
        val actual = lexAllToStrings("""x = 3.5; y = "hi";""")
        val expected = listOf(
            "ID(x)", "OP(ASSIGN)", "NUM(3.5)", "SEP(SEMICOLON)",
            "ID(y)", "OP(ASSIGN)", "STR(\"hi\")", "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun simboloDesconocidoDevuelveFailure_v1() {
        val err = lexError("let x = 5 $;")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }
}
