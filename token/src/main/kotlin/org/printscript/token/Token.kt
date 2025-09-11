package org.printscript.token

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.common.Type

sealed interface Token {
    val span: Span
}

sealed interface WordLikeToken : Token

data class BooleanLiteralToken(val value: Boolean, override val span: Span) : Token {
    override fun toString() = "BOOL($value)"
}

data class EofToken(override val span: Span) : Token {
    override fun toString() = "EOF"
}

data class IdentifierToken(val identifier: String, override val span: Span) : WordLikeToken {
    override fun toString() = "ID($identifier)"
}

data class KeywordToken(val kind: Keyword, override val span: Span) : WordLikeToken {
    override fun toString() = "KW($kind)"
}

data class NumberLiteralToken(val raw: String, override val span: Span) : WordLikeToken {
    override fun toString() = "NUM($raw)"
}

data class OperatorToken(val operator: Operator, override val span: Span) : Token {
    override fun toString() = "OP($operator)"
}

data class SeparatorToken(val separator: Separator, override val span: Span) : Token {
    override fun toString() = "SEP($separator)"
}

data class StringLiteralToken(val literal: String, override val span: Span) : WordLikeToken {
    override fun toString() = "STR(\"$literal\")"
}

data class TypeToken(val type: Type, override val span: Span) : WordLikeToken {
    override fun toString() = "TYPE($type)"
}
