package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions
import org.printscript.runner.Formatting
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.helpers.FormatterOptionsLoader
import org.printscript.runner.tokenStream

/**
 * Runner usado por el CLI:
 * - Carga configuración desde un path (io.configPath).
 * - Permite overridear indent con --indent.
 */
class FormatRunner(private val overrideIndent: Int? = null) : RunningMethod<String> {

    override fun run(version: Version, io: ProgramIo): Result<String, RunnerError> {
        // cargar opciones base desde config (o defaults)
        val base: FormatterOptions = FormatterOptionsLoader.fromPath(io.configPath?.toString())

        // aplicar override de indent si corresponde
        val opts: FormatterOptions = when {
            overrideIndent == null -> base
            base is FormatterConfig -> base.copy(indentSpaces = overrideIndent)
            else -> object : FormatterOptions by base {
                override val indentSpaces: Int = overrideIndent
            }
        }

        // wiring
        val w = LanguageWiringFactory.forVersion(version, formatterOptions = opts)

        val ts = tokenStream(io, w)

        val out = StringBuilder()
        return when (val fr = w.formatter.format(ts, out)) {
            is Success -> Success(out.toString())
            is Failure -> Failure(RunnerError(Formatting, "format error", fr.error))
        }
    }
}
