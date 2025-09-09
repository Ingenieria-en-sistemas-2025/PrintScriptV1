import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Lexerv0Test {

    private fun createTokenizer(src: String): Tokenizer {
        val cfg = PrintScriptv0MapConfig()
        return Tokenizer(src, cfg.rules(), cfg.triviaRules())
    }

    private fun lexAllToStrings(src: String): List<String> {
        val tokenizer = createTokenizer(src)
        return when (val r = tokenizer.tokenize()) {
            is Success -> r.value.map { it.toString() } // incluye EOF al final
            is Failure -> fail("Lexing failure: ${r.error.message} @ ${r.error.span}")
        }
    }

    private fun lexError(src: String): LexerError {
        val tz = createTokenizer(src)
        return when (val r = tz.tokenize()) {
            is Success -> fail("Se esperaba Failure, pero se obtuvo Success con ${r.value.size} tokens")
            is Failure -> r.error
        }
    }

    @Test
    fun letConString() {
        val src = """let name: string = "Joe";"""
        val actual = lexAllToStrings(src)

        val expected = listOf(
            "KW(LET)",
            "ID(name)",
            "SEP(COLON)",
            "TYPE(STRING)",
            "OP(ASSIGN)",
            "STR(\"Joe\")",
            "SEP(SEMICOLON)",
            "EOF",
        )

        assertEquals(expected, actual)
    }

    @Test
    fun asignacionesYNumerosEnterosYDecimales() {
        val actual = lexAllToStrings("a = 3; b = 3.14;")
        val expected = listOf(
            "ID(a)", "OP(ASSIGN)", "NUM(3)", "SEP(SEMICOLON)",
            "ID(b)", "OP(ASSIGN)", "NUM(3.14)", "SEP(SEMICOLON)", "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun expresionConParentesisYOperadores() {
        val actual = lexAllToStrings("println((a + b) / 2);")
        val expected = listOf(
            "KW(PRINTLN)", "SEP(LPAREN)",
            "SEP(LPAREN)", "ID(a)", "OP(PLUS)", "ID(b)", "SEP(RPAREN)",
            "OP(DIVIDE)", "NUM(2)",
            "SEP(RPAREN)", "SEP(SEMICOLON)", "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun longestMatchIdentificadorNoKeyword() {
        val actual = lexAllToStrings("letX=1;")
        val expected = listOf(
            "ID(letX)",
            "OP(ASSIGN)",
            "NUM(1)",
            "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun stringConComillasSimples() {
        val actual = lexAllToStrings("let s: string = 'hi';")
        val expected = listOf(
            "KW(LET)",
            "ID(s)",
            "SEP(COLON)",
            "TYPE(STRING)",
            "OP(ASSIGN)",
            "STR(\"hi\")",
            "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun triviaComentariosYEspacios() {
        val actual = lexAllToStrings(
            """
            // comentario
            let a: number = 12;   // otro
            println(a);
            """.trimIndent(),
        )
        val expected = listOf(
            "KW(LET)", "ID(a)", "SEP(COLON)", "TYPE(NUMBER)",
            "OP(ASSIGN)", "NUM(12)", "SEP(SEMICOLON)",
            "KW(PRINTLN)", "SEP(LPAREN)", "ID(a)", "SEP(RPAREN)", "SEP(SEMICOLON)",
            "EOF",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun simboloDesconocidoDevuelveFailure() {
        val err = lexError("let x = 5 $;")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }

    @Test
    fun ejemploConsiga1() {
        val actual = lexAllToStrings(
            """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """.trimIndent(),
        )
        // sanity general
        assertTrue(actual.contains("KW(LET)"))
        assertTrue(actual.contains("KW(PRINTLN)"))
        assertEquals("EOF", actual.last())
    }
}
