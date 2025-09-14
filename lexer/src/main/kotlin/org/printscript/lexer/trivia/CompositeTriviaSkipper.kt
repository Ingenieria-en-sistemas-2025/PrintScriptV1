package org.printscript.lexer.trivia

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

@Suppress("NestedBlockDepth")
class CompositeTriviaSkipper(
    private val rules: List<TriviaRule>,
) : TriviaSkipper {

    override fun peek(scanner: Scanner): Result<TriviaHit?, LexerError> {
        scanner.pinHere()
        try {
            for (rule in rules) {
                when (val r = rule.probe(scanner)) {
                    is Failure -> return r
                    is Success -> if (r.value != null) return r
                }
            }
            return Success(null)
        } finally {
            scanner.unpinHere()
        }
    }

    override fun skipAll(scanner: Scanner): Result<Scanner, LexerError> {
        var current = scanner
        while (!current.isEof) {
            when (val p = peek(current)) {
                is Failure -> return p
                is Success -> {
                    val hit = p.value ?: return Success(current)
                    if (hit.length == 0) return Success(current)
                    current = current.advance(hit.length)
                }
            }
        }
        return Success(current)
    }
}
