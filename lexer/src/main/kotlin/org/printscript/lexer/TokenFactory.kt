package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.lexingrules.RuleKey
import org.printscript.lexer.tokencreators.TokenCreator
import org.printscript.token.Token

class TokenFactory(
    private val creators: Map<RuleKey, TokenCreator>,
) {
    fun create(key: RuleKey, lexeme: Lexeme): Result<Token, LexerError> {
        val c = creators[key] ?: return Failure(
            LabeledError.of(lexeme.span, "internal: no TokenCreator for rule '$key'"),
        )
        return Success(c.create(lexeme))
    }
}
