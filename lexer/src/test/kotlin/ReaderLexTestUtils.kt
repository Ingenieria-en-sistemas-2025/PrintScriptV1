import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.LexingConfigFactory
import org.printscript.lexer.triviarules.CompositeTriviaSkipper
import org.printscript.token.Token
import java.io.StringReader
import kotlin.test.fail

object ReaderLexTestUtils {

    data class FeedOptions(
        val maxWindowCapacity: Int = 16, // ventana chica para forzar compaction
        val chunkSize: Int = 4, // chunks chicos para forzar bordes
        val keepTail: Int = 4,
    )

    private fun createTokenizer(
        version: Version,
        src: String,
        opts: FeedOptions = FeedOptions(),
    ): Tokenizer {
        val cfg = LexingConfigFactory.forVersion(version)
        val matcher = LongestMatchTokenMatcher(cfg.rules)
        val skipper = CompositeTriviaSkipper(cfg.trivia)
        val factory = TokenFactory(cfg.creators)

        val reader = StringReader(src).buffered(opts.maxWindowCapacity)

        return Tokenizer.of(reader, matcher, skipper, factory)
    }

    fun lexAllReader(version: Version, src: String, opts: FeedOptions = FeedOptions()): List<Token> {
        val tz = createTokenizer(version, src, opts)
        return when (val r = TokenCollector.collectAll(tz)) {
            is Success -> r.value
            is Failure -> fail("Lexing failure: ${r.error.message} @ ${r.error.span}")
        }
    }

    fun lexError(version: Version, src: String, opts: FeedOptions = FeedOptions()): LabeledError {
        val tz = createTokenizer(version, src, opts)
        return when (val r = TokenCollector.collectAll(tz)) {
            is Success -> fail("Se esperaba Failure, obtuvimos ${r.value.size} tokens")
            is Failure -> r.error
        }
    }
}
