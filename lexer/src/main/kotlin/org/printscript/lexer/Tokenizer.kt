package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.memory.CharFeed
import org.printscript.lexer.trivia.TriviaHit
import org.printscript.lexer.trivia.TriviaKind
import org.printscript.lexer.trivia.TriviaSkipper
import org.printscript.token.BlockCommentToken
import org.printscript.token.EofToken
import org.printscript.token.LineCommentToken
import org.printscript.token.NewlineToken
import org.printscript.token.Token
import org.printscript.token.WhitespaceToken
import java.io.Reader

class Tokenizer private constructor(
    private val scanner: Scanner,
    private val matcher: TokenMatcher,
    private val triviaSkipper: TriviaSkipper,
    private val tokenFactory: TokenFactory,
    private val mode: LexingMode,
) {
    companion object {
        fun of(src: String, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory, mode: LexingMode = LexingMode.SkipTrivia): Tokenizer =
            Tokenizer(Scanner(src), matcher, skipper, factory, mode)
        // Para archivos grandes (streaming real):

        fun of(reader: Reader, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory, mode: LexingMode = LexingMode.SkipTrivia): Tokenizer =
            Tokenizer(Scanner(reader), matcher, skipper, factory, mode)

        fun of(feed: CharFeed, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory, mode: LexingMode = LexingMode.SkipTrivia): Tokenizer =
            Tokenizer(Scanner(feed), matcher, skipper, factory, mode)
    }

    fun next(): Result<Pair<Token, Tokenizer>, LexerError> {
        when (mode) {
            LexingMode.EmitTrivia -> {
                when (val p = triviaSkipper.peek(scanner)) {
                    is Failure -> return p
                    is Success -> {
                        val hit = p.value
                        if (hit != null && hit.length > 0) {
                            return emitTriviaDirect(hit)
                        }
                    }
                }
            }
            LexingMode.SkipTrivia -> {
                when (val s = triviaSkipper.skipAll(scanner)) {
                    is Failure -> return s
                    is Success -> {
                        val after = s.value
                        if (after.isEof) return Success(buildEof(after))
                        return matchRegularToken(after)
                    }
                }
            }
        }
        if (scanner.isEof) return Success(buildEof(scanner))
        return matchRegularToken(scanner)
    }

    private fun buildEof(sEnd: Scanner): Pair<Token, Tokenizer> {
        val position = sEnd.position()
        val tok = EofToken(Span(position, position))
        return tok to Tokenizer(sEnd, matcher, triviaSkipper, tokenFactory, mode)
    }

    private fun buildMatchedToken(
        sBefore: Scanner,
        m: Match.Success,
    ): Result<Pair<Token, Tokenizer>, LexerError> {
        val text = sBefore.slice(m.length).toString()
        val sAfter = sBefore.advance(m.length)

        val span = Span(m.start, sAfter.position())
        val lexeme = Lexeme(text, span)

        return when (val r = tokenFactory.create(m.key, lexeme)) {
            is Success -> Success(r.value to Tokenizer(sAfter, matcher, triviaSkipper, tokenFactory, mode))
            is Failure -> Failure(r.error)
        }
    }

    private fun emitTriviaDirect(hit: TriviaHit): Result<Pair<Token, Tokenizer>, LexerError> {
        val sBefore = scanner
        val sAfter = sBefore.advance(hit.length)
        val span = Span(sBefore.position(), sAfter.position())
        val tok: Token = when (hit.kind) {
            TriviaKind.WHITESPACE -> WhitespaceToken(hit.raw, span)
            TriviaKind.NEWLINE -> NewlineToken(span)
            TriviaKind.LINE_COMMENT -> LineCommentToken(hit.raw, span)
            TriviaKind.BLOCK_COMMENT -> BlockCommentToken(hit.raw, span)
        }
        return Success(tok to Tokenizer(sAfter, matcher, triviaSkipper, tokenFactory, mode))
    }

    private fun matchRegularToken(from: Scanner): Result<Pair<Token, Tokenizer>, LexerError> {
        from.pinHere()
        return try {
            when (val m = matcher.matchNext(from)) {
                is Match.Success -> buildMatchedToken(from, m)
                is Match.Failure -> Failure(m.reason)
            }
        } finally {
            from.unpinHere()
        }
    }
}
