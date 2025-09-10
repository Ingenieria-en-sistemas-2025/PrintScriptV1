package org.printscript.lexer.error

import org.printscript.common.Span

data class UnexpectedChar(override val span: Span, val char: Char) : LexerError {
    override val message = "Símbolo inesperado: '$char'"
}
