package org.printscript.interpreter.errors

import org.printscript.common.Span
import org.printscript.common.Type

class IncompatibleType(override val span: Span, val expected: Type, val actual: String) : InterpreterError {
    override val message = "Tipo incompatible: esperaba $expected, obtuve $actual"
}
