package org.printscript.lexer.error

import org.printscript.common.Span

data class UnterminatedString(
    override val span: Span,
    override val message: String = "Cadena sin cerrar",
) : RuntimeException(message), LexerError
