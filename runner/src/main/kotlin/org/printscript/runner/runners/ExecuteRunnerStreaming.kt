package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.tokenStream

class ExecuteRunnerStreaming(
    private val printer: ((String) -> Unit)?, // va llamando a printer cada vez que hay algo para mostrar
    private val collectAlsoWithPrinter: Boolean = false, // permite coleccionar internamente
) : RunningMethod<Unit> {

    override fun run(version: Version, io: ProgramIo): Result<Unit, RunnerError> {
        val wiring = LanguageWiringFactory.forVersion(
            version,
            printer = printer,
            collectAlsoWithPrinter = collectAlsoWithPrinter,
        )

        val tokenStream = try { tokenStream(io, wiring) } catch (e: Exception) { return Failure(RunnerError(Stage.Lexing, "lexing failed", e)) }

        val stmts = try { wiring.statementStreamFromTokens(tokenStream) } catch (e: Exception) { return Failure(RunnerError(Stage.Parsing, "parsing failed", e)) }

        val interpreter = wiring.interpreterFor(io.inputProviderOverride)

        return try {
            when (val exec = interpreter.run(stmts)) {
                is Success -> Success(Unit)
                is Failure -> Failure(RunnerError(Stage.Interpreting, "runtime error", exec.error as? Throwable))
            }
        } catch (e: Exception) {
            Failure(RunnerError(Stage.Interpreting, "unexpected runtime failure", e))
        }
    }
}
