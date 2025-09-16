package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.Interpreting
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.tokenStream

// permite inyectar un printer (para imprimir en vivo) y opcionalmente coleccionar tmb
class ExecuteRunnerStreaming(
    private val printer: ((String) -> Unit)?, // va llamando a printer cada vez que hay algo para mostrar
    private val collectAlsoWithPrinter: Boolean = false, // permite coleccionar internamente
) : RunningMethod<Unit> {

    override fun run(version: Version, io: ProgramIo): Result<Unit, RunnerError> {
        val languageWiring = LanguageWiringFactory.forVersion(
            version,
            printer = printer,
            collectAlsoWithPrinter = collectAlsoWithPrinter,
        )

        // Tokeniza y produce el stream de statements
        val tokenStream = tokenStream(io, languageWiring)
        val statementStream = languageWiring.statementStreamFromTokens(tokenStream)
        val interpreter = languageWiring.interpreterFor(io.inputProviderOverride)

        return when (val execResult = interpreter.run(statementStream)) {
            is Success -> Success(Unit)
            is Failure -> Failure(RunnerError(Interpreting, "runtime error", execResult.error))
        }
    }
}
