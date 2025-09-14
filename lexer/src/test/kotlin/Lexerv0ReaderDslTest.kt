
import ReaderLexTestUtils.lexAllReader
import Utilities.assertLexEqualsWithBuilder
import Utilities.toView
import org.printscript.common.Keyword
import org.printscript.common.Version
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Lexerv0ReaderDslTest {

    private val stress = ReaderLexTestUtils.FeedOptions(
        maxWindowCapacity = 12,
        chunkSize = 5,
        keepTail = 4,
    )

    private fun lexAllTokens(src: String) = lexAllReader(Version.V0, src, stress)

    private fun lexError(src: String) = ReaderLexTestUtils.lexError(Version.V0, src, stress)

    @Test
    fun letConString() = assertLexEqualsWithBuilder(src = """let name: string = "Joe";""", ::lexAllTokens) {
        kw().let().identifier("name").sep().colon().ty().stringType().op().assign().string("Joe").sep().semicolon()
    }

    @Test
    fun asignacionesYNumerosEnterosYDecimales() = assertLexEqualsWithBuilder(src = "a = 3; b = 3.14;", ::lexAllTokens) {
        identifier("a").op().assign().number("3").sep().semicolon()
            .identifier("b").op().assign().number("3.14").sep().semicolon()
    }

    @Test
    fun expresionConParentesisYOperadores() = assertLexEqualsWithBuilder(src = "println((a + b) / 2);", ::lexAllTokens) {
        kw().println().sep().lparen()
            .sep().lparen().identifier("a").op().plus().identifier("b").sep().rparen()
            .op().divide().number("2")
            .sep().rparen().sep().semicolon()
    }

    @Test
    fun longestMatchIdentificadorNoKeyword() = assertLexEqualsWithBuilder(src = "letX=1;", ::lexAllTokens) {
        identifier("letX").op().assign().number("1").sep().semicolon()
    }

    @Test
    fun stringConComillasSimples() = assertLexEqualsWithBuilder(src = "let s: string = 'hi';", ::lexAllTokens) {
        kw().let().identifier("s").sep().colon().ty().stringType().op().assign().string("hi").sep().semicolon()
    }

    @Test
    fun triviaComentariosYEspacios() = assertLexEqualsWithBuilder(
        src = """
            // comentario
            let a: number = 12;   // otro
            println(a);
        """.trimIndent(),
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("a").sep().colon().ty().numberType().op().assign().number("12").sep().semicolon()
            .kw().println().sep().lparen().identifier("a").sep().rparen().sep().semicolon()
    }

    @Test
    fun simboloDesconocidoDevuelveFailure() {
        val err = lexError("let x = 5 $;")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }

    @Test
    fun ejemploConsigna1_sanity() {
        val actual = lexAllTokens(
            """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """.trimIndent(),
        ).map { it.toView() }

        assertTrue(actual.any { it.kind == "KW" && it.text == Keyword.LET.name })
        assertTrue(actual.any { it.kind == "KW" && it.text == Keyword.PRINTLN.name })
        assertEquals("EOF", actual.last().kind)
    }

    @Test
    fun tokenCruzaBorde_chunk_boundary_ID_largo() {
        // “identifierLong” forzará a cruzar bordes de chunk/ventana con los tamaños chicos
        val toks = lexAllTokens("let identifierLong: number = 1;").map { it.toString() }
        assertTrue(toks.contains("ID(identifierLong)"))
        assertTrue(toks.contains("TYPE(NUMBER)"))
    }

    @Test
    fun comentarioCruzaBorde_ySeSaltea() {
        val src = "let a: number = 1; // comentario muy largo que cruza chunk\nprintln(a);"
        val toks = lexAllTokens(src).map { it.toString() }
        // No debería haber tokens de comentario; sólo los esperados
        assertTrue(toks.first() == "KW(LET)")
        assertTrue(toks.contains("KW(PRINTLN)"))
    }
}
