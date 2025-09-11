package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.lexingrules.RuleKey

sealed interface Match {

    data class Success(val key: RuleKey, val start: Position, val length: Int) : Match
    data class Failure(val reason: LexerError) : Match
}
