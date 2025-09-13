package org.printscript.interpreter

import org.printscript.ast.Binary
import org.printscript.ast.Expression
import org.printscript.ast.Grouping
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.ReadEnv
import org.printscript.ast.ReadInput
import org.printscript.ast.Variable
import org.printscript.common.Failure
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.interpreter.errors.ExpectedStringForEnvName
import org.printscript.interpreter.errors.ExpectedStringForPrompt
import org.printscript.interpreter.errors.InterpreterError
import org.printscript.interpreter.errors.InvalidNumericLiteral
import org.printscript.interpreter.errors.UndeclaredVariable

class DefaultExprEvaluator(
    private val ops: Map<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>> = ExprHelpers.defaultBinaryOps,
) : ExprEvaluator {
    override fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError> =
        when (expr) {
            is LiteralNumber -> {
                val n = expr.raw.toDoubleOrNull()
                    ?: return Failure(InvalidNumericLiteral(expr.span, expr.raw))
                Success(Value.Num(n))
            }
            is LiteralString -> Success(Value.Str(expr.value))
            is Variable -> {
                val b = env.lookup(expr.name) // busca el env
                    ?: return Failure(UndeclaredVariable(expr.span, expr.name))
                Success(b.value) // si existe succes sino fail
            }
            is Grouping -> evaluate(expr.expression, env) // evalua recursivo adentro

            is LiteralBoolean -> Success(Value.Bool(expr.value))

            is Binary -> {
                val leftR = evaluate(expr.left, env)
                val rightR = evaluate(expr.right, env)
                leftR.flatMap { l ->
                    rightR.flatMap { r ->
                        // Usamos la tabla de operadores: extensible y testeable
                        ExprHelpers.applyBinaryOp(ops, expr.operator, expr.span, l, r)
                    }
                }
            }

            is ReadEnv -> {
                val varNameResult: Result<Value, InterpreterError> = evaluate(expr.variableName, env)

                varNameResult.flatMap { evaluatedName: Value ->
                    when (evaluatedName) {
                        is Value.Str -> {
                            val name: String = evaluatedName.s
                            val binding = env.lookup(name)
                                ?: return@flatMap Failure(UndeclaredVariable(expr.span, name))
                            Success(binding.value)
                        }
                        else -> Failure(
                            ExpectedStringForEnvName(expr.span, ExprHelpers.typeName(evaluatedName)),
                        )
                    }
                }
            }

            is ReadInput -> {
                val promptResult: Result<Value, InterpreterError> = evaluate(expr.prompt, env)

                promptResult.flatMap { evaluatedPrompt: Value ->
                    when (evaluatedPrompt) {
                        is Value.Str -> {
                            val raw: String = env.readInput(evaluatedPrompt.s)
                            Success(Value.Str(raw))
                        }
                        else -> Failure(
                            ExpectedStringForPrompt(expr.span, ExprHelpers.typeName(evaluatedPrompt)),
                        )
                    }
                }
            }

            // else -> Failure(InternalRuntimeError(expr.span, "Not supported expr: $expr"))
        }
}
