package org.printscript.ast

import org.printscript.common.Operator
import org.printscript.common.Span

sealed interface Expression { val span: Span }

data class Binary(
    val left: Expression,
    val right: Expression,
    val operator: Operator,
    val span: Span,
) : Expression

data class Grouping(
    val expression: Expression,
    override val span: Span,
) : Expression

data class LiteralBoolean(
    val value: Boolean,
    override val span: Span,
) : Expression

data class LiteralNumber(
    val raw: String,
    override val span: Span,
) : Expression

data class LiteralString(
    val value: String,
    override val span: Span,
) : Expression

data class ReadEnv(
    val variableName: Expression, // Literal String
    override val span: Span,
) : Expression

data class ReadInput(
    val prompt: Expression, // Literal String o variable
    override val span: Span,
) : Expression

data class Variable(
    val name: String,
    override val span: Span,
) : Expression
