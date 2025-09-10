package org.printscript.formatter.rules
import org.printscript.common.Operator
import org.printscript.token.OperatorToken
import org.printscript.token.Token

class BinaryOperatorSpacingRule : FormattingRule {
    private val binaryOps = setOf(
        Operator.PLUS,
        Operator.MINUS,
        Operator.MULTIPLY,
        Operator.DIVIDE,
    )

    private fun isBinaryOperator(t: Token?) =
        t is OperatorToken && t.operator in binaryOps

    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (isBinaryOperator(current)) return " " // antes del op
        if (isBinaryOperator(prev)) return " " // despues del op
        return null
    }
}
