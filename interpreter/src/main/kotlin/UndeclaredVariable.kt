package org.example

import Span


data class UndeclaredVariable(override val span: Span, val name: String) : InterpreterError {
    override val message = "Variable no declarada: '$name'"
}