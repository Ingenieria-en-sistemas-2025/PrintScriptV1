package org.printscript.ast

import org.printscript.common.Span

data class LiteralNumber(
    val raw: String,
    override val span: Span,
) : Expression
