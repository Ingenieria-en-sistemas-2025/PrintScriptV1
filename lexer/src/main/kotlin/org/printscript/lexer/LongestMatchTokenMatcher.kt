package org.printscript.lexer

import org.printscript.common.Span
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.lexingrules.LexingRule

class LongestMatchTokenMatcher(private val rules: List<LexingRule>) : TokenMatcher {

    override fun matchNext(scanner: Scanner): Match {
        val input = scanner.remainingFrom()
        var bestLen = 0
        var bestRule: LexingRule? = null

        for (r in rules) {
            val len = r.matchLength(input)
            if (len > bestLen) {
                bestLen = len
                bestRule = r
            }
        }

        if (bestLen > 0) return Match.Success(bestRule!!.key, scanner.position(), bestLen)

        val start = scanner.position()
        val after = scanner.advance(1)
        return Match.Failure(UnexpectedChar(Span(start, after.position()), scanner.peek()))
    }
}
