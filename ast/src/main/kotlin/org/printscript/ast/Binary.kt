package org.printscript.ast

import org.printscript.common.Operator
import org.printscript.common.Span

data class Binary(
    val left: Expression,
    val right: Expression,
    val operator: Operator,
    override val span: Span,
) : Expression
