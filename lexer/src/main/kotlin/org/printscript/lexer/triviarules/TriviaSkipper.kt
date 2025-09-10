package org.printscript.lexer.triviarules

import org.printscript.common.Result
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

interface TriviaSkipper {
    fun skipAll(scanner: Scanner): Result<Scanner, LexerError>
}
