package org.printscript.interpreter

import org.printscript.common.Version

object GlobalInterpreterFactory {
    fun forVersion(version: Version, inputOverride: InputProvider? = null): Interpreter {
        val input = inputOverride ?: when (version) {
            Version.V0 -> NoInputProvider
            Version.V1 -> StdInProvider
        }
        val env = Env.empty(input)
        val evaluator = DefaultExprEvaluator()
        val executor = StmtActionExecutor(evaluator)
        return ProgramInterpreter(executor, initialEnv = env, initialOut = Output.empty())
    }
}
