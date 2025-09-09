package org.printscript.ast

import org.printscript.common.Span

data class LiteralString(
    val value: String,
    override val span: Span,
) : Expression
