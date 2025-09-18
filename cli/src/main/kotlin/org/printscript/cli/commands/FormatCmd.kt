package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.printscript.cli.CliSupport
import org.printscript.cli.CommonOptions
import org.printscript.cli.ProgressSpinner
import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.ProgramIo
import org.printscript.runner.runners.FormatRunnerStreaming
import java.io.IOException

class FormatCmd : CliktCommand(
    name = "format",
    help = "Formatea el código fuente",
) {
    private val common by CommonOptions()
    private val indent by option("--indent", help = "Cantidad de espacios por indentación").int() // sobreescribe la cantidad de espacios de indentación definida en la config

    override fun run() {
        val version = CliSupport.resolveVersion(common.version)
        val spinner = ProgressSpinner("Formateando")
        spinner.start()
        try {
            when (val r = doFormat(version)) {
                is Success -> {
                    // no hay que hacer echo del string porque ya se escribió a System.out
                }
                is Failure -> {
                    val e = r.error
                    echo("Format error (${e.stage}): ${e.message}")
                    e.cause?.let { echo("  causa: $it") }
                }
            }
        } catch (ioe: IOException) {
            echo("Error formateando (IO): ${ioe.message}")
        } catch (iae: IllegalArgumentException) {
            echo("Error formateando (argumentos): ${iae.message}")
        } finally {
            spinner.stop()
        }
    }

    private fun doFormat(version: Version) =
        java.nio.file.Files.newBufferedReader(common.file).use { reader ->
            val io = ProgramIo(
                reader = reader,
                configPath = common.config,
            )
            val runner = FormatRunnerStreaming(
                out = System.out,
                overrideIndent = indent,
            )
            runner.run(version, io)
        }
}
