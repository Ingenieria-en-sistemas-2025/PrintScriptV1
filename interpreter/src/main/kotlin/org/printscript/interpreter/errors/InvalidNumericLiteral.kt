package org.printscript.interpreter.errors

import org.printscript.common.Span

data class InvalidNumericLiteral(
    override val span: Span,
    val lexeme: String,
) : InterpreterError {
    override val message: String = "Número inválido: '$lexeme'"
}
