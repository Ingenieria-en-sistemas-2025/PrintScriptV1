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

class TokenBuilder private constructor(
    private val tokens: List<Token>, // inmutable
    private val dummySpan: Span = Span(Position(1, 1), Position(1, 1)),
) {

    constructor() : this(emptyList())

    fun keyword(kw: Keyword): TokenBuilder =
        TokenBuilder(tokens + KeywordToken(kw, dummySpan))

    fun identifier(name: String): TokenBuilder =
        TokenBuilder(tokens + IdentifierToken(name, dummySpan))

    fun number(value: String): TokenBuilder =
        TokenBuilder(tokens + NumberLiteralToken(value, dummySpan))

    fun string(value: String): TokenBuilder =
        TokenBuilder(tokens + StringLiteralToken(value, dummySpan))

    fun operator(op: Operator): TokenBuilder =
        TokenBuilder(tokens + OperatorToken(op, dummySpan))

    fun separator(sep: Separator): TokenBuilder =
        TokenBuilder(tokens + SeparatorToken(sep, dummySpan))

    fun type(type: Type): TokenBuilder =
        TokenBuilder(tokens + TypeToken(type, dummySpan))

    fun boolean(value: Boolean): TokenBuilder =
        TokenBuilder(tokens + BooleanLiteralToken(value, dummySpan))

    fun build(): TokenStream =
        ListTokenStream.of(tokens + EofToken(dummySpan))
}
