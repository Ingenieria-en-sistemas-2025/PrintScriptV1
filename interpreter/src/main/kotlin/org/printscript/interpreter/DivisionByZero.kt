package org.printscript.interpreter

import org.printscript.common.Span

data class DivisionByZero(override val span: Span) : InterpreterError {
    override val message: String = "Error: division by zero"
}
