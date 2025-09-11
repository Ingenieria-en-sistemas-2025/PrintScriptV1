package org.printscript.ast

import org.printscript.common.Span

data class ReadEnv(
    val variableName: Expression, // Literal String
    override val span: Span,
) : Expression
