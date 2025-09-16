
import Utilities.assertLexEqualsWithBuilder
import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.PrintScriptv0MapConfig
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.trivia.CompositeTriviaSkipper
import org.printscript.token.Token
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class Lexerv0DiffFromV1DslTest {
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
    fun trueFalseSonIdentificadoresEnV0() = assertLexEqualsWithBuilder(
        src = "true false trueValue",
        lexActual = ::lexAllTokens,
    ) {
        identifier("true").identifier("false").identifier("trueValue")
    }

    @Test
    fun constEsIdentificadorEnV0() = assertLexEqualsWithBuilder(
        src = "const x: number = 1;",
        lexActual = ::lexAllTokens,
    ) {
        identifier("const")
            .identifier("x").sep().colon().ty().numberType()
            .op().assign().number("1").sep().semicolon()
    }

    @Test
    fun readInputYReadEnvSonIdentifiersEnV0() = assertLexEqualsWithBuilder(
        src =
        """
        let name: string = readInput("Name: ");
        let debug: string = readEnv("DEBUG");
        """.trimIndent(),
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("name").sep().colon().ty().stringType().op().assign()
            .identifier("readInput").sep().lparen().string("Name: ").sep().rparen().sep().semicolon()
            .kw().let().identifier("debug").sep().colon().ty().stringType().op().assign()
            .identifier("readEnv").sep().lparen().string("DEBUG").sep().rparen().sep().semicolon()
    }

    @Test
    fun ifElseYLlavesNoSoportadosEnV0_disparaUnexpectedCharPorLlave() {
        val err = lexError("""if (x) { println("ok"); }""")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }
}
