package org.printscript.token

private fun quotePs(s: String): String = buildString {
    append('"')
    s.forEach { ch ->
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            else -> append(ch)
        }
    }
    append('"')
}

val Token.codeText: String
    get() = when (this) {
        is IdentifierToken -> identifier
        is NumberLiteralToken -> raw
        is StringLiteralToken -> quotePs(literal) // comillas
        is BooleanLiteralToken -> if (value) "true" else "false"

        is KeywordToken -> kind.string
        is TypeToken -> type.value
        is OperatorToken -> operator.symbol
        is SeparatorToken -> separator.value

        else -> ""
    }
