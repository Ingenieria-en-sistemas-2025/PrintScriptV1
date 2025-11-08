package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.helpers.FormatterOptionsLoader
import org.printscript.runner.tokenStream

/**
 * Runner usado por el CLI:
 * - Carga configuración desde un path (io.configPath).
 * - Permite overridear indent con --indent.
 */

// Lee la configuración desde el filesystem, permite overridear indent con --indent,
// y formatea directo a out (sin String intermedio).

class FormatRunnerStreaming(
    private val out: Appendable,
    private val overrideIndent: Int? = null,
) : RunningMethod<Unit> {

    override fun run(version: Version, io: ProgramIo): Result<Unit, RunnerError> {
        val baseOptions: FormatterOptions = FormatterOptionsLoader.fromPath(io.configPath?.toString())

        val effectiveOptions: FormatterOptions = when {
            overrideIndent == null -> baseOptions
            baseOptions is FormatterConfig -> baseOptions.copy(indentSpaces = overrideIndent)
            else -> object : FormatterOptions by baseOptions { override val indentSpaces: Int = overrideIndent }
        }

        val wiring = LanguageWiringFactory.forVersion(version, formatterOptions = effectiveOptions)

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
