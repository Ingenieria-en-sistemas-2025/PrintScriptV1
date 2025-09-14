import Utilities.assertLexEqualsWithBuilder
import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.PrintScriptv1MapConfig
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

class Lexerv1DslTest {

    private fun createTokenizer(src: String): Tokenizer {
        val cfg = PrintScriptv1MapConfig()
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
    fun constBooleanTrueLiteral() = assertLexEqualsWithBuilder(
        src = """const flag: boolean = true;""",
        lexActual = ::lexAllTokens,
    ) {
        kw().const()
            .identifier("flag")
            .sep().colon()
            .ty().booleanType()
            .op().assign()
            .boolean(true)
            .sep().semicolon()
    }

    @Test
    fun ifElseConLlavesYPrintln() = assertLexEqualsWithBuilder(
        src = """if (flag) { println("ok"); } else { println("no"); }""",
        lexActual = ::lexAllTokens,
    ) {
        kw().ifkey().sep().lparen().identifier("flag").sep().rparen()
            .sep().lbrace()
            .kw().println().sep().lparen().string("ok").sep().rparen().sep().semicolon()
            .sep().rbrace()
            .kw().elsekey()
            .sep().lbrace()
            .kw().println().sep().lparen().string("no").sep().rparen().sep().semicolon()
            .sep().rbrace()
    }

    @Test
    fun readInputYReadEnvComoKeywords() = assertLexEqualsWithBuilder(
        src =
        """
        let name: string = readInput("Name: ");
        let debug: boolean = readEnv("DEBUG");
        """.trimIndent(),
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("name").sep().colon().ty().stringType().op().assign()
            .kw().readInput().sep().lparen().string("Name: ").sep().rparen().sep().semicolon()
            .kw().let().identifier("debug").sep().colon().ty().booleanType().op().assign()
            .kw().readEnv().sep().lparen().string("DEBUG").sep().rparen().sep().semicolon()
    }

    @Test
    fun trueFalseSonLiteralsNoIdentificadores() = assertLexEqualsWithBuilder(
        src = "true false trueValue",
        lexActual = ::lexAllTokens,
    ) {
        boolean(true).boolean(false).identifier("trueValue")
    }

    @Test
    fun numerosYStringsSiguenIgual() = assertLexEqualsWithBuilder(
        src = """x = 3.5; y = "hi";""",
        lexActual = ::lexAllTokens,
    ) {
        identifier("x").op().assign().number("3.5").sep().semicolon()
            .identifier("y").op().assign().string("hi").sep().semicolon()
    }

    @Test
    fun simboloDesconocidoDevuelveFailure_v1() {
        val err = lexError("let x = 5 $;")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }
}
