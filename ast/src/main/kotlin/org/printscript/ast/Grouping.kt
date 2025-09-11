package org.printscript.ast

import org.printscript.common.Span

data class Grouping(
    val expression: Expression,
    override val span: Span,
) : Expression
