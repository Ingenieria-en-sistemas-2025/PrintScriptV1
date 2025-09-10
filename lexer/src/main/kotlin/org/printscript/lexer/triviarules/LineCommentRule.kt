package org.printscript.lexer.triviarules

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

object LineCommentRule : TriviaRule {
    override fun matchLen(scanner: Scanner): Result<Int, LexerError> {
        val remaining = scanner.remainingFrom(0)
        if (!remaining.startsWith("//")) return Success(0)
        var i = 2
        while (i < remaining.length && remaining[i] != '\n') i++
        // no consumimos el '\n' (queda para la siguiente pasada)
        return Success(i) // incluye hasta justo antes del '\n'
    }
}
