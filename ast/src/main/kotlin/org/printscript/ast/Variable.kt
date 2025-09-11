package org.printscript.ast

import org.printscript.common.Span

data class Variable(
    val name: String,
    override val span: Span,
) : Expression
