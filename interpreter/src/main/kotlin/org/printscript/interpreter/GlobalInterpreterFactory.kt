package org.printscript.interpreter

import org.printscript.common.Version

object GlobalInterpreterFactory {
    fun forVersion(
        version: Version,
        inputOverride: InputProvider? = null,
        printer: Printer? = null,
        collectAlsoWithPrinter: Boolean = false,
    ): Interpreter {
        val input = inputOverride ?: when (version) {
            Version.V0 -> NoInputProvider
            Version.V1 -> StdInProvider
        }

        val env = Env.empty(input).withPrinter(printer, collectAlsoWithPrinter)

        val evaluator = DefaultExprEvaluator()
        val executor = StmtActionExecutor(evaluator)

        // Tu Output sigue siendo el collector puro; no hace falta sink.
        return ProgramInterpreter(
            executor,
            initialEnv = env,
            initialOut = Output.empty(),
        )
    }
}
