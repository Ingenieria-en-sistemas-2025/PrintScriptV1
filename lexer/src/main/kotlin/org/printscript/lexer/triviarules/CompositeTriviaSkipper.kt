package org.printscript.lexer.triviarules

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

@Suppress("NestedBlockDepth")
class CompositeTriviaSkipper(
    private val rules: List<TriviaRule>,
) : TriviaSkipper {

    override fun skipAll(scanner: Scanner): Result<Scanner, LexerError> {
        // Pinneamos el scanner inicial (posición actual) y lo despinneamos siempre.
        scanner.pinHere()
        try {
            var current = scanner

            while (!current.isEof) {
                when (val step = consumeOneTrivia(current)) {
                    is Failure -> return step
                    is Success -> {
                        val advanceBy = step.value
                        if (advanceBy == 0) return Success(current)
                        current = current.advance(advanceBy)
                    }
                }
            }
            return Success(current)
        } finally {
            scanner.unpinHere()
        }
    }

    private fun consumeOneTrivia(scanner: Scanner): Result<Int, LexerError> {
        for (rule in rules) {
            when (val r = rule.matchLen(scanner)) {
                is Failure -> return r
                is Success -> if (r.value > 0) return r
            }
        }
        return Success(0)
    }
}
