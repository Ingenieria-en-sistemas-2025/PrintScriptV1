package org.example

import Span

class InvalidPrintValue( override val span: Span): InterpreterError {
    override val message = "Valor inválido para imprimir"
}