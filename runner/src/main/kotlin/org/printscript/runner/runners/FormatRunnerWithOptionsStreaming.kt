package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterOptions
import org.printscript.runner.Formatting
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.tokenStream

/**
 * Runner usado en adapters (ej. TCK):
 * recibe directamente un FormatterOptions ya construido (stream/bytes/etc).
 */

// Usa exactamente el objeto FormatterOptions que ya le pasaste (en memoria).
// No lee archivos, no aplica overrides. Formatea directo a out.
class FormatRunnerWithOptionsStreaming(
    private val out: Appendable,
    private val options: FormatterOptions,
) : RunningMethod<Unit> {

    override fun run(version: Version, io: ProgramIo): Result<Unit, RunnerError> {
        val wiring = LanguageWiringFactory.forVersion(version, formatterOptions = options)
        val tokens = tokenStream(io, wiring)

        return when (val fmtResult = wiring.formatter.format(tokens, out)) {
            is Success -> Success(Unit)
            is Failure -> Failure(RunnerError(Formatting, "format error", fmtResult.error))
        }
    }
}
