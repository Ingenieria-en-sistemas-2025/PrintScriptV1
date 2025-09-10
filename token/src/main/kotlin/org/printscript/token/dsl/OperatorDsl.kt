package org.printscript.token.dsl

import org.printscript.common.Operator

fun TokenBuilder.op() = OperatorDsl(this)
class OperatorDsl(private val b: TokenBuilder) {
    fun assign() = b.operator(Operator.ASSIGN)
    fun plus() = b.operator(Operator.PLUS)
    fun minus() = b.operator(Operator.MINUS)
    fun multiply() = b.operator(Operator.MULTIPLY)
    fun divide() = b.operator(Operator.DIVIDE)
}
