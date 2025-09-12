package org.printscript.interpreter.errors

import org.printscript.common.Span

data class UndeclaredVariable(override val span: Span, val name: String) : InterpreterError {
    override val message = "Variable no declarada: '$name'"
}
