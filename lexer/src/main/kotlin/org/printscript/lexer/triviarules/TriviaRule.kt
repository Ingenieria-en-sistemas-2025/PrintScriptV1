package org.printscript.lexer.triviarules

import org.printscript.common.Result
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

// Reglas que consumen caracteres sin emitir tokens
interface TriviaRule {

    fun matchLen(scanner: Scanner): Result<Int, LexerError>
}
