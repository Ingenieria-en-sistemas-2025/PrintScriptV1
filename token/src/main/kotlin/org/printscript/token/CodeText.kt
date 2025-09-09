package org.printscript.token

val Token.codeText: String
    get() = when (this) {
        is IdentifierToken -> identifier
        is NumberLiteralToken -> raw
        is StringLiteralToken -> literal

        is KeywordToken -> kind.string
        is TypeToken -> type.value
        is OperatorToken -> operator.symbol
        is SeparatorToken -> separator.value

        is EofToken -> "" // no se imprime
        else -> ""
    }
