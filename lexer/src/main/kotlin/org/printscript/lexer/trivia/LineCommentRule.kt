package org.printscript.lexer.trivia

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

object LineCommentRule : TriviaRule {
    override fun probe(scanner: Scanner): Result<TriviaHit?, LexerError> {
        val rem = scanner.remainingFrom(0)
        if (rem.length < 2 || rem[0] != '/' || rem[1] != '/') return Success(null)

        var i = 2
        while (i < rem.length && rem[i] != '\n' && rem[i] != '\r') i++
        return Success(TriviaHit(i, TriviaKind.LINE_COMMENT, rem.subSequence(0, i).toString()))
    }
}
