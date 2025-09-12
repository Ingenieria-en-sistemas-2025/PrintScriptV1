package org.printscript.ast

import org.printscript.common.Span
import org.printscript.common.Type

sealed interface Statement { val span: Span }

data class Assignment(
    val name: String,
    val value: Expression,
    override val span: Span,
) : Statement

data class ConstDeclaration(
    val name: String,
    val type: Type,
    val initializer: Expression,
    override val span: Span,
) : Statement

data class IfStmt(
    val condition: Expression,
    val thenBranch: List<Statement>,
    val elseBranch: List<Statement>?, // null si no hay else
    override val span: Span,
) : Statement

data class Println(
    val value: Expression,
    override val span: Span,
) : Statement

data class VarDeclaration(
    val name: String,
    val type: Type,
    val initializer: Expression?,
    override val span: Span,
) : Statement
