package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.memory.CharFeed
import org.printscript.lexer.triviarules.TriviaSkipper
import org.printscript.token.EofToken
import org.printscript.token.Token
import java.io.Reader

class Tokenizer private constructor(
    private val scanner: Scanner,
    private val matcher: TokenMatcher,
    private val triviaSkipper: TriviaSkipper,
    private val tokenFactory: TokenFactory,
) {
    companion object {
        // Para archivos grandes (streaming real):
        fun of(reader: Reader, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory): Tokenizer =
            Tokenizer(Scanner(reader), matcher, skipper, factory)

        fun of(lines: Iterator<String>, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory): Tokenizer =
            Tokenizer(Scanner(lines), matcher, skipper, factory)

        fun of(feed: CharFeed, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory): Tokenizer =
            Tokenizer(Scanner(feed), matcher, skipper, factory)

        // Para tests chicos:
        fun of(src: String, matcher: TokenMatcher, skipper: TriviaSkipper, factory: TokenFactory): Tokenizer =
            Tokenizer(Scanner(src), matcher, skipper, factory)
    }

    fun next(): Result<Pair<Token, Tokenizer>, LexerError> {
        return when (val triviaResult = triviaSkipper.skipAll(scanner)) {
            is Failure -> triviaResult
            is Success -> {
                val scannerAfterTrivia = triviaResult.value
                if (scannerAfterTrivia.isEof) return Success(buildEof(scannerAfterTrivia))

                scannerAfterTrivia.pinHere()
                val matchResult = matcher.matchNext(scannerAfterTrivia)
                scannerAfterTrivia.unpinHere()

                when (matchResult) {
                    is Match.Success -> buildMatchedToken(scannerAfterTrivia, matchResult)
                    is Match.Failure -> Failure(matchResult.reason)
                }
            }
        }
    }

    private fun buildEof(sEnd: Scanner): Pair<Token, Tokenizer> {
        val position = sEnd.position()
        val tok = EofToken(Span(position, position))
        return tok to Tokenizer(sEnd, matcher, triviaSkipper, tokenFactory)
    }

    private fun buildMatchedToken(sBefore: Scanner, m: Match.Success): Result<Pair<Token, Tokenizer>, LexerError> {
        val sAfter = sBefore.advance(m.length)
        val text = sBefore.slice(m.length).toString()
        val span = Span(m.start, sAfter.position())
        val lexeme = Lexeme(text, span)
        return when (val r = tokenFactory.create(m.key, lexeme)) {
            is Success -> Success(r.value to Tokenizer(sAfter, matcher, triviaSkipper, tokenFactory))
            is Failure -> Failure(r.error)
        }
    }
}
