package org.printscript.interpreter

import org.printscript.common.Version

object GlobalInterpreterFactory {
    fun forVersion(
        version: Version,
        inputOverride: InputProvider? = null,
        printer: Printer? = null,
    ): Interpreter {
        val input = inputOverride ?: when (version) {
            Version.V0 -> NoInputProvider
            Version.V1 -> StdInProvider
        }
        val env = Env.empty(input).withPrinter(printer)
        val evaluator = DefaultExprEvaluator()
        val executor = StmtActionExecutor(evaluator)

        val out = if (printer != null) Output.sink() else Output.empty()

        return ProgramInterpreter(
            executor,
            initialEnv = env,
            initialOut = out,
        )
    }
}
