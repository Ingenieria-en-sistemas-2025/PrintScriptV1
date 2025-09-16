package org.printscript.lexer.tokencreators

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.token.OperatorToken
import org.printscript.token.Token

class OperatorTokenCreator(
    private val ops: Map<String, Operator>,
) : TokenCreator {
    override fun create(lexeme: Lexeme): Result<Token, LexerError> {
        val op = ops[lexeme.text]
            ?: return Failure(
                LabeledError.of(
                    lexeme.span,
                    "internal: OperatorRule produced unknown lexeme '${lexeme.text}'",
                ),
            )
        return Success(OperatorToken(op, lexeme.span))
    }
}
