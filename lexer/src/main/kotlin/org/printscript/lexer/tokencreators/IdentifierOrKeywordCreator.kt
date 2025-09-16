package org.printscript.lexer.tokencreators

import org.printscript.common.Keyword
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Type
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.Token
import org.printscript.token.TypeToken

class IdentifierOrKeywordCreator(
    private val keywords: Map<String, Keyword>,
    private val types: Map<String, Type>,
) : TokenCreator {
    override fun create(lexeme: Lexeme): Result<Token, LexerError> {
        types[lexeme.text]?.let { return Success(TypeToken(it, lexeme.span)) }
        keywords[lexeme.text]?.let { return Success(KeywordToken(it, lexeme.span)) }
        return Success(IdentifierToken(lexeme.text, lexeme.span))
    }
}
