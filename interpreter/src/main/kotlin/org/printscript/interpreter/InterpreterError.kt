package org.printscript.interpreter

import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Span
import org.printscript.common.Type

typealias InterpreterError = LabeledError

data class DivisionByZero(override val span: Span) : InterpreterError {
    override val message: String = "Error: division by zero"
}

class IncompatibleType(override val span: Span, val expected: Type, val actual: String) : InterpreterError {
    override val message = "Tipo incompatible: esperaba $expected, obtuve $actual"
}

class InternalRuntimeError(override val span: Span, override val message: String) : InterpreterError

data class InvalidNumericLiteral(
    override val span: Span,
    val lexeme: String,
) : InterpreterError {
    override val message: String = "Número inválido: '$lexeme'"
}

class InvalidPrintValue(override val span: Span) : InterpreterError {
    override val message = "Valor inválido para imprimir"
}

data class Redeclaration(override val span: Span, val name: String) : InterpreterError {
    override val message = "Variable ya declarada: '$name'"
}

data class UndeclaredVariable(override val span: Span, val name: String) : InterpreterError {
    override val message = "Variable no declarada: '$name'"
}

class UnsupportedBinaryOp(override val span: Span, val operator: Operator, val leftType: String, val rightType: String) : InterpreterError {
    override val message =
        "Operación no soportada: $leftType ${operator.name} $rightType"
}
