package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.Interpreting
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError

class ExecuteRunnerStreaming(
    private val printer: ((String) -> Unit)?, // nullable
    private val collectAlsoWithPrinter: Boolean = false, // ⬅️ NUEVO
) : RunningMethod<Unit> {

    override fun run(version: Version, io: ProgramIo): Result<Unit, RunnerError> {
        val w = LanguageWiringFactory.forVersion(
            version,
            printer = printer,
            collectAlsoWithPrinter = collectAlsoWithPrinter,
        )

        val ts = tokenStream(io, w)
        val stmts = w.statementStreamFromTokens(ts)
        val interpreter = w.interpreterFor(io.inputProviderOverride)

        return when (val rr = interpreter.run(stmts)) {
            is Success -> Success(Unit)
            is Failure -> Failure(RunnerError(Interpreting, "runtime error", rr.error))
        }
    }
}
