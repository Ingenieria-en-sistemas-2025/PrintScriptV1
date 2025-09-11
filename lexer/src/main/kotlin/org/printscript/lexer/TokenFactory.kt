package org.printscript.lexer

import org.printscript.lexer.lexingrules.RuleKey
import org.printscript.lexer.tokencreators.TokenCreator
import org.printscript.token.Token

class TokenFactory(private val creators: Map<RuleKey, TokenCreator>) {
    fun create(key: RuleKey, lexeme: Lexeme): Token =
        creators[key]?.create(lexeme)
            ?: error("No TokenCreator for rule '${key.id}'")
}
