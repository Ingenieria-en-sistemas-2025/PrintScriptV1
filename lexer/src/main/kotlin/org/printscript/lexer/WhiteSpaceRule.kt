package org.printscript.lexer

import org.printscript.common.Result
import org.printscript.common.Success

object WhiteSpaceRule : TriviaRule {
    override fun matchLen(scanner: Scanner): Result<Int, LexerError> {
        val rem = scanner.remaining()
        var i = 0
        while (i < rem.length && rem[i].isWhitespace()) i++
        return Success(i) // 0 si no hay espacios
    }
}
