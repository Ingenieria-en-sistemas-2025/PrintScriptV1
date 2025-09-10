package org.printscript.ast

import org.printscript.common.Span

sealed interface Expression { val span: Span }

// class InvalidExpression(override val span: Span) : ast.Expression // para test de interpreter
