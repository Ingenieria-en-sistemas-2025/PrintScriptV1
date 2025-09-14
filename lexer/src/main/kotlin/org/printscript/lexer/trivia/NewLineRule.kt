package org.printscript.lexer.trivia

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

object NewLineRule : TriviaRule {
    override fun probe(scanner: Scanner): Result<TriviaHit?, LexerError> {
        val rem = scanner.remainingFrom(0)
        if (rem.isEmpty()) return Success(null)

        val len = when {
            rem.length >= 2 && rem[0] == '\r' && rem[1] == '\n' -> 2
            rem[0] == '\n' || rem[0] == '\r' -> 1
            else -> 0
        }
        return if (len == 0) {
            Success(null)
        } else {
            Success(TriviaHit(len, TriviaKind.NEWLINE, rem.subSequence(0, len).toString()))
        }
    }
}
