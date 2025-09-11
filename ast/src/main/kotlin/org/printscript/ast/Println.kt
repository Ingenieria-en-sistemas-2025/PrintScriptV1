package org.printscript.ast

import org.printscript.common.Span

data class Println(
    val value: Expression,
    override val span: Span,
) : Statement
