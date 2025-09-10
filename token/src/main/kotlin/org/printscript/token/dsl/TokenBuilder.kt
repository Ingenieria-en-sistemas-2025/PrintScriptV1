package org.printscript.token.dsl

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Position
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.common.Type
import org.printscript.token.BooleanLiteralToken
import org.printscript.token.EofToken
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.ListTokenStream
import org.printscript.token.NumberLiteralToken
import org.printscript.token.OperatorToken
import org.printscript.token.SeparatorToken
import org.printscript.token.StringLiteralToken
import org.printscript.token.Token
import org.printscript.token.TokenStream
import org.printscript.token.TypeToken

class TokenBuilder {
    private val tokens = mutableListOf<Token>()
    private val dummySpan = Span(Position(1, 1), Position(1, 1))

    fun keyword(kw: Keyword): TokenBuilder {
        tokens.add(KeywordToken(kw, dummySpan))
        return this
    }

    fun identifier(name: String): TokenBuilder {
        tokens.add(IdentifierToken(name, dummySpan))
        return this
    }

    fun number(value: String): TokenBuilder {
        tokens.add(NumberLiteralToken(value, dummySpan))
        return this
    }

    fun string(value: String): TokenBuilder {
        tokens.add(StringLiteralToken(value, dummySpan))
        return this
    }

    fun operator(op: Operator): TokenBuilder {
        tokens.add(OperatorToken(op, dummySpan))
        return this
    }

    fun separator(sep: Separator): TokenBuilder {
        tokens.add(SeparatorToken(sep, dummySpan))
        return this
    }

    fun type(type: Type): TokenBuilder {
        tokens.add(TypeToken(type, dummySpan))
        return this
    }

    fun boolean(value: Boolean): TokenBuilder {
        tokens.add(BooleanLiteralToken(value, dummySpan))
        return this
    }

    fun build(): TokenStream {
        val eof = EofToken(dummySpan)
        return ListTokenStream.of(tokens + eof)
    }
}
