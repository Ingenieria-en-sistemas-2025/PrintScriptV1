import Utilities.assertLexEqualsWithBuilder
import Utilities.toView
import org.printscript.common.Failure
import org.printscript.common.Keyword
import org.printscript.common.Success
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.PrintScriptv0MapConfig
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.triviarules.CompositeTriviaSkipper
import org.printscript.token.Token
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class Lexerv0DslTest {

    private fun createTokenizer(src: String): Tokenizer {
        val cfg = PrintScriptv0MapConfig()
        val matcher = LongestMatchTokenMatcher(cfg.rules())
        val skipper = CompositeTriviaSkipper(cfg.triviaRules())
        val factory = TokenFactory(cfg.creators())
        return Tokenizer.of(src, matcher, skipper, factory)
    }

    private fun lexAllTokens(src: String): List<Token> {
        val tz = createTokenizer(src)
        return when (val r = TokenCollector.collectAll(tz)) {
            is Success -> r.value
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
    fun letConString() = assertLexEqualsWithBuilder(
        src = """let name: string = "Joe";""",
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("name")
            .sep().colon()
            .ty().stringType()
            .op().assign()
            .string("Joe")
            .sep().semicolon()
    }

    @Test
    fun asignacionesYNumerosEnterosYDecimales() = assertLexEqualsWithBuilder(
        src = "a = 3; b = 3.14;",
        lexActual = ::lexAllTokens,
    ) {
        identifier("a").op().assign().number("3").sep().semicolon()
            .identifier("b").op().assign().number("3.14").sep().semicolon()
    }

    @Test
    fun expresionConParentesisYOperadores() = assertLexEqualsWithBuilder(
        src = "println((a + b) / 2);",
        lexActual = ::lexAllTokens,
    ) {
        kw().println().sep().lparen()
            .sep().lparen().identifier("a").op().plus().identifier("b").sep().rparen()
            .op().divide().number("2")
            .sep().rparen().sep().semicolon()
    }

    @Test
    fun longestMatchIdentificadorNoKeyword() = assertLexEqualsWithBuilder(
        src = "letX=1;",
        lexActual = ::lexAllTokens,
    ) {
        identifier("letX").op().assign().number("1").sep().semicolon()
    }

    @Test
    fun stringConComillasSimples() = assertLexEqualsWithBuilder(
        src = "let s: string = 'hi' ;",
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("s").sep().colon().ty().stringType()
            .op().assign().string("hi").sep().semicolon()
    }

    @Test
    fun triviaComentariosYEspacios() = assertLexEqualsWithBuilder(
        src =
        """
        // comentario
        let a: number = 12;   // otro
        println(a);
        """.trimIndent(),
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("a").sep().colon().ty().numberType()
            .op().assign().number("12").sep().semicolon()
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
        kotlin.test.assertEquals("EOF", actual.last().kind)
    }
}
