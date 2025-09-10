package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.lexingrules.LexingRule

class LongestMatchTokenMatcher(private val rules: List<LexingRule>) : TokenMatcher {

    override fun matchNext(scanner: Scanner): Match {
        return when (val matchResult = findBestMatch(scanner)) {
            is Success -> buildSuccess(scanner, matchResult.value)
            is Failure -> Match.Failure(matchResult.error)
        }
    }

    private fun findBestMatch(scanner: Scanner): Result<BestMatch, LexerError> {
        val input = scanner.remainingFrom(0)
        var bestLength = 0
        var bestRule: LexingRule? = null

        for (rule in rules) {
            val currentLength = rule.matchLength(input)
            if (currentLength > bestLength) {
                bestLength = currentLength
                bestRule = rule
            }
        }

        return if (bestLength > 0) {
            Success(BestMatch(bestRule!!, bestLength))
        } else {
            val startPosition = scanner.position()
            val scannerAfterError = scanner.advance(1)
            Failure(
                UnexpectedChar(
                    Span(startPosition, scannerAfterError.position()),
                    scanner.peek(),
                ),
            )
        }
    }

    private fun buildSuccess(scanner: Scanner, best: BestMatch): Match.Success =
        Match.Success(
            rule = best.rule,
            start = scanner.position(),
            length = best.length,
        )

    private data class BestMatch(val rule: LexingRule, val length: Int)
}
