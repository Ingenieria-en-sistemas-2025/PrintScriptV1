package org.printscript.ast

import org.printscript.common.Span

data class Assignment(
    val name: String,
    val value: Expression,
    override val span: Span,
) : Statement
