import Utilities.assertLexEqualsWithBuilder
import org.printscript.common.Success
import org.printscript.lexer.LexingMode
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.PrintScriptv0MapConfig
import org.printscript.lexer.trivia.CompositeTriviaSkipper
import org.printscript.token.Token
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import kotlin.test.Test
import kotlin.test.fail

class Lexerv0TriviaDslTest {

    private fun createTokenizer(src: String, mode: LexingMode = LexingMode.EmitTrivia): Tokenizer {
        val cfg = PrintScriptv0MapConfig()
        val matcher = LongestMatchTokenMatcher(cfg.rules())
        val skipper = CompositeTriviaSkipper(cfg.triviaRules())
        val factory = TokenFactory(cfg.creators())
        return Tokenizer.of(src, matcher, skipper, factory, mode)
    }

    private fun lexAllTokens(src: String, mode: LexingMode = LexingMode.EmitTrivia): List<Token> {
        val tz = createTokenizer(src, mode)
        return when (val r = TokenCollector.collectAll(tz)) {
            is Success -> r.value
            else -> fail("Lexing failure en test de trivia")
        }
    }

    @Test
    fun trivia_blockComment_simple() = assertLexEqualsWithBuilder(
        src = "/* bloque */",
        lexActual = { s -> lexAllTokens(s) },
    ) {
        tv().bcomment("/* bloque */")
    }

    @Test
    fun trivia_intercalada_entre_tokens() = assertLexEqualsWithBuilder(
        src = "let  a/*X*/=1; // fin\n",
        lexActual = { s -> lexAllTokens(s) },
    ) {
        // let
        kw().let()
            // 2 espacios
            .tv().ws("  ")
            // a
            .identifier("a")
            // bloque /*X*/
            .tv().bcomment("/*X*/")
            // '='
            .op().assign()
            // 1
            .number("1")
            // ';'
            .sep().semicolon()
            // espacio + // fin + newline
            .tv().ws(" ")
            .tv().lcomment("// fin")
            .tv().nl()
    }

    @Test
    fun trivia_newlines_variantes_unix_y_windows() = assertLexEqualsWithBuilder(
        src = "\n\r\n",
        lexActual = { s -> lexAllTokens(s) },
    ) {
        tv().nl() // \n
            .tv().nl() // \r\n
    }
}
