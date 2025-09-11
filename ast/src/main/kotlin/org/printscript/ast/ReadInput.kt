package org.printscript.ast

import org.printscript.common.Span

data class ReadInput(
    val prompt: Expression, // Literal String o variable
    override val span: Span,
) : Expression
