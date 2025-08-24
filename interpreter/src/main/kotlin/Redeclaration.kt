package org.example

import Span


data class Redeclaration(override val span: Span, val name: String) : InterpreterError {
    override val message = "Variable ya declarada: '$name'"
}