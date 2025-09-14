package org.printscript.lexer.config

import org.printscript.common.Version
import org.printscript.lexer.LexingMode
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.memory.BufferedStreamingTokenStream
import org.printscript.lexer.memory.ReaderChunkFeed
import org.printscript.lexer.trivia.CompositeTriviaSkipper
import org.printscript.token.TokenStream
import java.io.Reader

private const val DEFAULT_MAX_WINDOW_CAPACITY = 1 shl 15 // 32 KiB
private const val DEFAULT_CHUNK_SIZE = 1 shl 13 //  8 KiB
private const val DEFAULT_KEEP_TAIL = 1 shl 13 //  8 KiB

class LexerFactory {

    data class FeedOptions(
        val maxWindowCapacity: Int = DEFAULT_MAX_WINDOW_CAPACITY,
        val chunkSize: Int = DEFAULT_CHUNK_SIZE,
        val keepTail: Int = DEFAULT_KEEP_TAIL,
    )

    // tokenizer desde string
    @JvmOverloads
    fun tokenizer(version: Version, src: String, mode: LexingMode = LexingMode.SkipTrivia): Tokenizer {
        val cfg = LexingConfigFactory.forVersion(version)
        val matcher = LongestMatchTokenMatcher(cfg.rules)
        val skipper = CompositeTriviaSkipper(cfg.trivia)
        val factory = TokenFactory(cfg.creators)
        return Tokenizer.of(src, matcher, skipper, factory, mode)
    }

    // tokenizer desde reader, archivos, usando line iterator feed
    @JvmOverloads
    fun tokenizer(
        version: Version,
        reader: Reader,
        feedOptions: FeedOptions = FeedOptions(),
        mode: LexingMode = LexingMode.SkipTrivia,
    ): Tokenizer {
        val cfg = LexingConfigFactory.forVersion(version)
        val matcher = LongestMatchTokenMatcher(cfg.rules)
        val skipper = CompositeTriviaSkipper(cfg.trivia)
        val factory = TokenFactory(cfg.creators)

        val feed = ReaderChunkFeed(
            reader = reader.buffered(),
            maxWindowCapacity = feedOptions.maxWindowCapacity,
            chunkSize = feedOptions.chunkSize,
            keepTail = feedOptions.keepTail,
        )
        return Tokenizer.of(feed, matcher, skipper, factory, mode)
    }

    @JvmOverloads
    fun tokenStream(version: Version, src: String, emitTrivia: Boolean = false): TokenStream {
        val mode = if (emitTrivia) LexingMode.EmitTrivia else LexingMode.SkipTrivia
        val tz = tokenizer(version, src, mode)
        return BufferedStreamingTokenStream.of(tz)
    }

    @JvmOverloads
    fun tokenStream(
        version: Version,
        reader: Reader,
        feedOptions: FeedOptions = FeedOptions(),
        emitTrivia: Boolean = false,
    ): TokenStream {
        val mode = if (emitTrivia) LexingMode.EmitTrivia else LexingMode.SkipTrivia
        val tz = tokenizer(version, reader, feedOptions, mode)
        return BufferedStreamingTokenStream.of(tz)
    }
}
