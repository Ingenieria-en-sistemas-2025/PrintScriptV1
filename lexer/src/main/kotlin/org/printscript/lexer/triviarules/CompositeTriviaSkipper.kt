package org.printscript.lexer.triviarules

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

class CompositeTriviaSkipper(
    private val rules: List<TriviaRule>,
) : TriviaSkipper {

    override fun skipAll(scanner: Scanner): Result<Scanner, LexerError> {
        var currentScanner = scanner

        while (!currentScanner.isEof) {
            when (val advanceResult = consumeOneTrivia(currentScanner)) {
                is Failure -> return advanceResult // error de trivia (p.ej., bloque no cerrado)
                is Success -> {
                    val maybeStopped = advanceOrStop(currentScanner, advanceResult.value)
                    if (maybeStopped is Success && maybeStopped.value === currentScanner) {
                        return maybeStopped
                    }
                    currentScanner = (maybeStopped as Success).value
                }
            }
        }
        return Success(currentScanner) // EOF alcanzado
    }

    private fun consumeOneTrivia(scanner: Scanner): Result<Int, LexerError> {
        for (rule in rules) {
            when (val matchResult = rule.matchLen(scanner)) {
                is Failure -> return matchResult
                is Success -> if (matchResult.value > 0) return matchResult
            }
        }
        return Success(0)
    }

    private fun advanceOrStop(scanner: Scanner, advanceBy: Int): Result<Scanner, LexerError> =
        if (advanceBy == 0) Success(scanner) else Success(scanner.advance(advanceBy))
}
