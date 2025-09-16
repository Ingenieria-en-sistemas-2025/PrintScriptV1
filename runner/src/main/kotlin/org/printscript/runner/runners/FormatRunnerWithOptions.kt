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
class FormatRunnerWithOptions(private val options: FormatterOptions) : RunningMethod<String> {
    override fun run(version: Version, io: ProgramIo): Result<String, RunnerError> {
        val w = LanguageWiringFactory.forVersion(version, formatterOptions = options)
        val ts = tokenStream(io, w)

        val out = StringBuilder()
        return when (val fr = w.formatter.format(ts, out)) {
            is Success -> Success(out.toString())
            is Failure -> Failure(RunnerError(Formatting, "format error", fr.error))
        }
    }
}
