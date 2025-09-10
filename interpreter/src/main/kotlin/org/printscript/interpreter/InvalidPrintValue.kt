package org.printscript.interpreter

import org.printscript.common.Span

class InvalidPrintValue(override val span: Span) : InterpreterError {
    override val message = "Valor inválido para imprimir"
}
