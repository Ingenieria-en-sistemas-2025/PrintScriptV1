package rules

import Operator
import OperatorToken
import Token

class BinaryOperatorSpacingRule: FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is OperatorToken && current.operator in setOf(
                Operator.PLUS, Operator.MINUS, Operator.MULTIPLY, Operator.DIVIDE
            )) return " ${current.operator.symbol} "
        return null
    }
}