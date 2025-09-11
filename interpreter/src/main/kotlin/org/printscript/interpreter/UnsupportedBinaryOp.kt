package org.printscript.interpreter

import org.printscript.common.Operator
import org.printscript.common.Span

class UnsupportedBinaryOp(override val span: Span, val operator: Operator, val leftType: String, val rightType: String) : InterpreterError {
    override val message =
        "Operación no soportada: $leftType ${operator.name} $rightType"
}
