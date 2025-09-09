package org.printscript.ast

import org.printscript.common.Span
import org.printscript.common.Type

data class VarDeclaration(
    val name: String,
    val type: Type,
    val initializer: Expression?,
    override val span: Span,
) : Statement
