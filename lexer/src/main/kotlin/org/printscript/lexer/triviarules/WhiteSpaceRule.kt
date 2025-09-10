package org.printscript.lexer.triviarules

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

object WhiteSpaceRule : TriviaRule {
    override fun matchLen(scanner: Scanner): Result<Int, LexerError> {
        val remaining = scanner.remainingFrom(0)
        var i = 0
        while (i < remaining.length && remaining[i].isWhitespace()) i++
        return Success(i) // 0 si no hay espacios
    }
}
