package org.printscript.lexer.tokencreators

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Success
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class SeparatorTokenCreator(
    private val seps: Map<String, Separator>,
) : TokenCreator {
    override fun create(lexeme: Lexeme): Result<Token, LexerError> {
        val sep = seps[lexeme.text]
            ?: return Failure(
                LabeledError.of(
                    lexeme.span,
                    "internal: SeparatorRule produced unknown lexeme '${lexeme.text}'",
                ),
            )
        return Success(SeparatorToken(sep, lexeme.span))
    }
}
