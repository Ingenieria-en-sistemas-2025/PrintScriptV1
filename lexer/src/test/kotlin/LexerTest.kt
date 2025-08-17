
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LexerTest {

    private fun lexAllToStrings(src: String): List<String> {
        val tz = Tokenizer(src, PrintScriptMapConfig().rules())
        return tz.tokenize().map { it.toString() } // incluye EOF al final
    }

    @Test
    fun let_con_string() {
        val src = """let name: string = "Joe";"""
        val actual = lexAllToStrings(src)

        val expected = listOf(
            "KW(LET)", "ID(name)", "SEP(COLON)", "TYPE(STRING)",
            "OP(ASSIGN)", "STR(\"Joe\")", "SEP(SEMICOLON)", "EOF"
        )

        assertEquals(expected, actual)
    }

    @Test
    fun asignaciones_y_numeros_enteros_y_decimales() {
        val actual = lexAllToStrings("a = 3; b = 3.14;")
        val expected = listOf(
            "ID(a)", "OP(ASSIGN)", "NUM(3)", "SEP(SEMICOLON)",
            "ID(b)", "OP(ASSIGN)", "NUM(3.14)", "SEP(SEMICOLON)", "EOF"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun expresion_con_parentesis_y_operadores() {
        val actual = lexAllToStrings("println((a + b) / 2);")
        val expected = listOf(
            "KW(PRINTLN)", "SEP(LPAREN)",
            "SEP(LPAREN)", "ID(a)", "OP(PLUS)", "ID(b)", "SEP(RPAREN)",
            "OP(DIVIDE)", "NUM(2)",
            "SEP(RPAREN)", "SEP(SEMICOLON)", "EOF"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun longest_match_identificador_no_keyword() {
        val actual = lexAllToStrings("letX=1;")
        val expected = listOf(
            "ID(letX)", "OP(ASSIGN)", "NUM(1)", "SEP(SEMICOLON)", "EOF"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun string_con_comillas_simples() {
        val actual = lexAllToStrings("let s: string = 'hi';")
        val expected = listOf(
            "KW(LET)", "ID(s)", "SEP(COLON)", "TYPE(STRING)",
            "OP(ASSIGN)", "STR(\"hi\")", "SEP(SEMICOLON)", "EOF"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun trivia_comentarios_y_espacios() {
        val actual = lexAllToStrings(
            """
            // comentario
            let a: number = 12;   // otro
            println(a);
            """.trimIndent()
        )
        val expected = listOf(
            "KW(LET)", "ID(a)", "SEP(COLON)", "TYPE(NUMBER)",
            "OP(ASSIGN)", "NUM(12)", "SEP(SEMICOLON)",
            "KW(PRINTLN)", "SEP(LPAREN)", "ID(a)", "SEP(RPAREN)", "SEP(SEMICOLON)",
            "EOF"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun simbolo_desconocido_tira_lexerexception() {
        assertFailsWith<LexerException> {
            lexAllToStrings("let x = 5 $;")
        }
    }

    @Test
    fun ejemplo_consiga_1() {
        val actual = lexAllToStrings(
            """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """.trimIndent()
        )
        // sanity general
        assertTrue(actual.contains("KW(LET)"))
        assertTrue(actual.contains("KW(PRINTLN)"))
        assertEquals("EOF", actual.last())
    }
}