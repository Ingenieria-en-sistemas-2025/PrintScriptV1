package org.printscript.lexer.trivia

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

object WhiteSpaceRule : TriviaRule {
    override fun probe(scanner: Scanner): Result<TriviaHit?, LexerError> {
        val rem = scanner.remainingFrom(0)
        var i = 0
        while (i < rem.length) {
            val c = rem[i]
            if (c == ' ' || c == '\t' || c == '\u000C') i++ else break
        }
        return if (i == 0) {
            Success(null)
        } else {
            Success(TriviaHit(i, TriviaKind.WHITESPACE, rem.subSequence(0, i).toString()))
        }
    }
}
