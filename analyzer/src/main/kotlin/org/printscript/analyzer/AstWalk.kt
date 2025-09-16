package org.printscript.analyzer

import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.Expression
import org.printscript.ast.Grouping
import org.printscript.ast.IfStmt
import org.printscript.ast.Println
import org.printscript.ast.ReadEnv
import org.printscript.ast.ReadInput
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration

object AstWalk {

    private const val INITIAL_STACK_CAPACITY = 8 // para no tener "MagicNumber"

    // Recorre las expresiones de un Statement en preorden, sin corutinas.
    // La secuencia no recorre nada hasta que alguien la itera.
    fun expressionsOf(stmt: Statement): Sequence<Expression> =
        object : Sequence<Expression> {
            override fun iterator(): Iterator<Expression> = ExprIterator(stmt)
        }

    // Iterador iterativo (stack) que evita secuencias/continuations.
    private class ExprIterator(root: Statement) : kotlin.collections.AbstractIterator<Expression>() {
        // La pila contiene Statements o Expressions pendientes de visitar.
        private val stack = ArrayDeque<Any>(INITIAL_STACK_CAPACITY).apply { addLast(root) } // solo el root

        override fun computeNext() {
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()

                if (node is Expression) {
                    pushExprChildren(node)
                    setNext(node) // yieldea el propio nodo -> produce (emite) este elemento y devuelve el control al consumidor.
                    return
                }

                if (node is Statement) {
                    pushStmtChildren(node)
                    continue
                }
            }
            done()
        }

        private fun pushExprChildren(e: Expression) {
            when (e) {
                is Binary -> {
                    stack.addLast(e.right)
                    stack.addLast(e.left)
                }
                is Grouping -> stack.addLast(e.expression)
                is ReadInput -> stack.addLast(e.prompt)
                is ReadEnv -> stack.addLast(e.variableName)
                else -> { /* Variable, literales, etc.: sin hijos */ }
            }
        }

        private fun pushStmtChildren(s: Statement) {
            when (s) {
                is VarDeclaration -> s.initializer?.let { stack.addLast(it) }
                is ConstDeclaration -> stack.addLast(s.initializer)
                is Assignment -> stack.addLast(s.value)
                is Println -> stack.addLast(s.value)
                is IfStmt -> {
                    // push en orden inverso porque la pila es LIFO:
                    s.elseBranch?.asReversed()?.forEach { stack.addLast(it) }
                    s.thenBranch.asReversed().forEach { stack.addLast(it) }
                    stack.addLast(s.condition)
                }
                else -> {}
            }
        }
    }
}
