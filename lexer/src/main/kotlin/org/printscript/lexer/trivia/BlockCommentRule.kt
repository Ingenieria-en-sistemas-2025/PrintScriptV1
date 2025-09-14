package org.printscript.lexer.trivia

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnterminatedCommentBlock

object BlockCommentRule : TriviaRule {
    override fun probe(scanner: Scanner): Result<TriviaHit?, LexerError> {
        val rem = scanner.remainingFrom(0)
        if (rem.length < 2 || rem[0] != '/' || rem[1] != '*') return Success(null)

        // Buscamos '*/'
        var i = 2
        while (i < rem.length - 1) {
            if (rem[i] == '*' && rem[i + 1] == '/') {
                val consumed = i + 2
                val raw = rem.subSequence(0, consumed).toString()
                return Success(TriviaHit(consumed, TriviaKind.BLOCK_COMMENT, raw))
            }
            i++
        }

        // Unterminated: reportamos error con Span desde el inicio hasta EOF visible
        val startPos = scanner.position()
        val endPos = scanner.advance(rem.length).position()
        return Failure(UnterminatedCommentBlock(Span(startPos, endPos)))
    }
}
