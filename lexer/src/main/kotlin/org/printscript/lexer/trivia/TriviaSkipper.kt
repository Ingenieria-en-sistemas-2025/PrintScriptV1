package org.printscript.lexer.trivia

import org.printscript.common.Result
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

interface TriviaSkipper {
    fun skipAll(scanner: Scanner): Result<Scanner, LexerError>
    fun peek(scanner: Scanner): Result<TriviaHit?, LexerError>
}
