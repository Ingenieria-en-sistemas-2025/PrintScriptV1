package org.printscript.lexer.trivia

import org.printscript.common.Result
import org.printscript.lexer.Scanner
import org.printscript.lexer.error.LexerError

// Reglas que consumen caracteres sin emitir tokens
interface TriviaRule {
    fun probe(scanner: Scanner): Result<TriviaHit?, LexerError>
}
