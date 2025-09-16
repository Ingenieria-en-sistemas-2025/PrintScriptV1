// cli/src/main/kotlin/org/printscript/cli/commands/AnalyzeCmd.kt
package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import org.printscript.cli.CliSupport
import org.printscript.cli.CommonOptions
import org.printscript.cli.ProgressSpinner
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.runner.ProgramIo
import org.printscript.runner.helpers.DiagnosticStringFormatter
import org.printscript.runner.runners.AnalyzeRunner
import java.io.IOException

class AnalyzeCmd : CliktCommand(
    name = "analyze",
    help = "Analiza y lista diagnósticos (warnings/errores) con config por defecto",
) {
    private val common by CommonOptions()

    override fun run() {
        val version = CliSupport.resolveVersion(common.version)
        val spinner = ProgressSpinner("Analizando")
        spinner.start()
        try {
            doAnalyze(version).fold(
                onSuccess = { list ->
                    if (list.isEmpty()) {
                        echo("Sin diagnósticos.")
                    } else {
                        list.forEach { d -> echo(DiagnosticStringFormatter.format(d)) }
                    }
                },
                onFailure = { e ->
                    echo("Analyze error (${e.stage}): ${e.message}")
                    e.cause?.let { echo("  causa: $it") }
                },
            )
        } catch (ioe: IOException) {
            echo("Error analizando (IO): ${ioe.message}")
        } catch (iae: IllegalArgumentException) {
            echo("Error analizando (argumentos): ${iae.message}")
        } finally {
            spinner.stop()
        }
    }

    private fun doAnalyze(version: Version): Result<List<org.printscript.analyzer.Diagnostic>, org.printscript.runner.RunnerError> =
        CliSupport.newReader(common.file).use { reader ->
            val io = ProgramIo(reader = reader)
            AnalyzeRunner.run(version, io)
        }
}
