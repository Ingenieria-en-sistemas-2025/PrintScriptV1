package org.printscript.analyzer

import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.Expression
import org.printscript.ast.Grouping
import org.printscript.ast.IfStmt
import org.printscript.ast.Println
import org.printscript.ast.ProgramNode
import org.printscript.ast.ReadEnv
import org.printscript.ast.ReadInput
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration

object AstWalk {

    fun statements(program: ProgramNode): Sequence<Statement> =
        program.statements.asSequence()

    fun expressionsOf(stmt: Statement): Sequence<Expression> = sequence {
        fun walkExpr(e: Expression): Sequence<Expression> = sequence {
            yield(e)
            when (e) {
                is Binary -> {
                    yieldAll(walkExpr(e.left))
                    yieldAll(walkExpr(e.right))
                }
                is Grouping -> yieldAll(walkExpr(e.expression))
                is ReadInput -> yieldAll(walkExpr(e.prompt))
                is ReadEnv -> yieldAll(walkExpr(e.variableName))
                else -> Unit
            }
        }

        when (stmt) {
            is VarDeclaration -> stmt.initializer?.let { yieldAll(walkExpr(it)) }
            is ConstDeclaration -> yieldAll(walkExpr(stmt.initializer))
            is Assignment -> yieldAll(walkExpr(stmt.value))
            is Println -> yieldAll(walkExpr(stmt.value))
            is IfStmt -> {
                yieldAll(walkExpr(stmt.condition))
                stmt.thenBranch.forEach { yieldAll(expressionsOf(it)) }
                stmt.elseBranch?.forEach { yieldAll(expressionsOf(it)) }
            }
            else -> Unit
        }
    }

    fun expressions(program: ProgramNode): Sequence<Expression> =
        statements(program).flatMap { expressionsOf(it) }

    inline fun <reified T : Expression> ofType(program: ProgramNode): Sequence<T> =
        expressions(program).filterIsInstance<T>()
}
