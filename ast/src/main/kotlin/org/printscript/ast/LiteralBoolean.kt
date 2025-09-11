package org.printscript.ast

import org.printscript.common.Span

data class LiteralBoolean(
    val value: Boolean,
    override val span: Span,
) : Expression
