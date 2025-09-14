package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.lexingrules.RuleKey
import org.printscript.lexer.tokencreators.TokenCreator
import org.printscript.token.Token

private data class InternalLexerError(
    override val span: Span,
    override val message: String,
) : RuntimeException(message), LexerError

class TokenFactory(
    private val creators: Map<RuleKey, TokenCreator>,
) {

    @Suppress("TooGenericExceptionCaught")
    fun create(key: RuleKey, lexeme: Lexeme): Result<org.printscript.token.Token, LexerError> {
        val c = creators[key] ?: return Failure(
            InternalLexerError(lexeme.span, "internal: no TokenCreator for rule '$key'"),
        )
        return try {
            Success(c.create(lexeme))
        } catch (e: Throwable) {
            when (e) {
                is LexerError -> Failure(e)
                else -> Failure(InternalLexerError(lexeme.span, e.message ?: "token creation failed"))
            }
        }
    }
}
