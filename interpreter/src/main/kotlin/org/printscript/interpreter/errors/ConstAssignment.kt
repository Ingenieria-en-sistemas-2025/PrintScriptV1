package org.printscript.interpreter.errors

import org.printscript.common.Span

data class ConstAssignment(override val span: Span, val name: String) : InterpreterError {
    override val message: String = "No se puede reasignar constante: '$name'"
}
