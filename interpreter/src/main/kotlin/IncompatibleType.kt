package org.example

import Span
import Type

class IncompatibleType(override val span: Span, val expected: Type, val actual: String): InterpreterError {
    override val message = "Tipo incompatible: esperaba $expected, obtuve $actual"
}