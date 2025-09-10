package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.triviarules.TriviaSkipper
import org.printscript.token.EofToken
import org.printscript.token.Token

class Tokenizer private constructor(
    private val scanner: Scanner,
    private val matcher: TokenMatcher,
    private val triviaSkipper: TriviaSkipper,
) {
    companion object {
        fun of(src: String, matcher: TokenMatcher, skipper: TriviaSkipper): Tokenizer =
            Tokenizer(Scanner(src), matcher, skipper)
    }

    fun next(): Result<Pair<Token, Tokenizer>, LexerError> {
        return when (val triviaResult = triviaSkipper.skipAll(scanner)) {
            is Failure -> triviaResult
            is Success -> {
                val scannerAfterTrivia = triviaResult.value
                if (scannerAfterTrivia.isEof) {
                    return Success(buildEof(scannerAfterTrivia))
                }

                return when (val matchResult = matcher.matchNext(scannerAfterTrivia)) {
                    is Match.Success -> Success(buildMatchedToken(scannerAfterTrivia, matchResult))
                    is Match.Failure -> Failure(matchResult.reason)
                }
            }
        }
    }

    private fun buildEof(scannerAtEnd: Scanner): Pair<Token, Tokenizer> {
        val eofPosition = scannerAtEnd.position()
        val eofToken = EofToken(Span(eofPosition, eofPosition))
        val nextTokenizer = Tokenizer(scannerAtEnd, matcher, triviaSkipper)
        return eofToken to nextTokenizer
    }

    private fun buildMatchedToken(
        scannerBeforeMatch: Scanner,
        match: Match.Success,
    ): Pair<Token, Tokenizer> {
        val scannerAfterMatch = scannerBeforeMatch.advance(match.length)
        val lexeme = scannerBeforeMatch.slice(match.length).toString()
        val span = Span(match.start, scannerAfterMatch.position())
        val token = match.rule.build(lexeme, span)
        val nextTokenizer = Tokenizer(scannerAfterMatch, matcher, triviaSkipper)
        return token to nextTokenizer
    }
}
