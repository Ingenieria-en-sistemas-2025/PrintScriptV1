package org.printscript.lexer.triviarules

import org.printscript.common.Failure
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnterminatedCommentBlock

object BlockCommentRule : TriviaRule {

    override fun matchLen(scanner: Scanner): Result<Int, LexerError> {
        if (!startsBlockComment(scanner)) return Success(0)
        val remaining = scanner.remainingFrom(0)
        val startPosition = scanner.position()
        val closingIndex = findClosingDelimiterIndex(remaining)

        if (closingIndex >= 0) {
            val consumed = lengthIncludingClosing(closingIndex) // i + 2
            return Success(consumed)
        }

        return unterminatedBlockFailure(scanner, remaining.length, startPosition)
    }

    private fun startsBlockComment(scanner: Scanner): Boolean {
        val rem = scanner.remainingFrom(0)
        return rem.length >= 2 && rem[0] == '/' && rem[1] == '*'
    }

    private fun findClosingDelimiterIndex(input: CharSequence): Int {
        var i = 2
        while (i < input.length - 1) {
            if (input[i] == '*' && input[i + 1] == '/') return i
            i++
        }
        return -1
    }

    private fun lengthIncludingClosing(closingIndex: Int): Int = closingIndex + 2

    private fun unterminatedBlockFailure(
        scannerAtStart: Scanner,
        remainingLength: Int,
        startPosition: Position,
    ): Failure<LexerError> {
        val endPosition = scannerAtStart.advance(remainingLength).position()
        return Failure(UnterminatedCommentBlock(Span(startPosition, endPosition)))
    }
}
