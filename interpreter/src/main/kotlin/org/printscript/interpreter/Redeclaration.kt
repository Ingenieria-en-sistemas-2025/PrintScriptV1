package org.printscript.interpreter

import org.printscript.common.Span

data class Redeclaration(override val span: Span, val name: String) : InterpreterError {
    override val message = "Variable ya declarada: '$name'"
}
