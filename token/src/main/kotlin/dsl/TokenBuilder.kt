package dsl

import EofToken
import IdentifierToken
import Keyword
import KeywordToken
import NumberLiteralToken
import Operator
import OperatorToken
import Position
import Separator
import SeparatorToken
import Span
import StringLiteralToken
import Token
import TokenStream
import Type
import TypeToken

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

    fun build(): TokenStream {
        val eof = EofToken(dummySpan)
        return ListTokenStream.of(tokens + eof)
    }
}
