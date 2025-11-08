package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterOptions
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.tokenStream

/**
 * Runner usado en adapters (ej. TCK):
 * recibe directamente un FormatterOptions ya construido (stream/bytes/etc).
 */

class FormatRunnerWithOptionsStreaming(
    private val out: Appendable,
    private val options: FormatterOptions,
) : RunningMethod<Unit> {

    override fun run(version: Version, io: ProgramIo): Result<Unit, RunnerError> {
        val wiring = LanguageWiringFactory.forVersion(version, formatterOptions = options)

        val tokens = try { tokenStream(io, wiring) } catch (e: Exception) { return Failure(RunnerError(Stage.Lexing, "lexing failed", e)) }

        return try {
            when (val fmt = wiring.formatter.format(tokens, out)) {
                is Success -> Success(Unit)
                is Failure -> Failure(RunnerError(Stage.Formatting, "format error", fmt.error as? Throwable))
            }
        } catch (e: Exception) {
            Failure(RunnerError(Stage.Formatting, "unexpected format failure", e))
        }
    }
}
