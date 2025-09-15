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

        val (env0, out0) =
            if (printer != null) {
                Env.empty(input).withPrinter(printer, true) to Output.sink()
            } else {
                Env.empty(input) to Output.empty()
            }

        val evaluator = DefaultExprEvaluator()
        val executor = StmtActionExecutor(evaluator)

        return ProgramInterpreter(
            executor = executor,
            initialEnv = env0,
            initialOut = out0,
        )
    }
}
