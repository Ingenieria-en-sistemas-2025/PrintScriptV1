package org.printscript.token

import org.printscript.common.Operator
import org.printscript.common.Span

data class OperatorToken(val operator: Operator, override val span: Span) : Token {
    override fun toString() = "OP($operator)"
}
