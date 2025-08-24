package org.example

import Operator
import Span

class UnsupportedBinaryOp(override val span: Span, val operator: Operator, val leftType: String, val rightType: String) : InterpreterError {
    override val message =
        "Operación no soportada: $leftType ${operator.name} $rightType"
}