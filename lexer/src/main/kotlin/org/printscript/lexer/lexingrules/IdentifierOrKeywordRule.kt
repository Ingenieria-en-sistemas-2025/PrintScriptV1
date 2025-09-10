package org.printscript.lexer.lexingrules

import org.printscript.common.Keyword
import org.printscript.common.Span
import org.printscript.common.Type
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.Token
import org.printscript.token.TypeToken

private val IDENT_REGEX = Regex("[A-Za-z_][A-Za-z0-9_]*")

class IdentifierOrKeywordRule(kw: Map<String, Keyword>, tp: Map<String, Type>) : LexingRule {

    private val keywords: Map<String, Keyword> = kw.toMap()
    private val types: Map<String, Type> = tp.toMap()

    override fun matchLength(input: CharSequence): Int =
        IDENT_REGEX.matchAt(input, 0)?.value?.length ?: 0

    override fun build(lexeme: String, span: Span): Token {
        types[lexeme]?.let { return TypeToken(it, span) }
        keywords[lexeme]?.let { return KeywordToken(it, span) }
        return IdentifierToken(lexeme, span)
    }
}
