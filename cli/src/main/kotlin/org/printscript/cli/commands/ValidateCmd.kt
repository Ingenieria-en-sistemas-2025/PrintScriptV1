package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import org.printscript.cli.CliSupport
import org.printscript.cli.CommonOptions
import org.printscript.cli.ProgressSpinner
import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.ProgramIo
import org.printscript.runner.helpers.DiagnosticStringFormatter
import org.printscript.runner.runners.Runner
import java.io.IOException
import java.nio.file.Files

class ValidateCmd : CliktCommand(
    name = "validate",
    help = "Valida estáticamente el código (sin ejecutar)",
) {
    private val common by CommonOptions()

    override fun run() {
        val version = CliSupport.resolveVersion(common.version)
        val spinner = ProgressSpinner("Validando")
        spinner.start()
        try {
            when (val res = doValidate(version)) {
                is Success -> {
                    val report = res.value
                    if (report.hasErrors) echo("Encontré errores:") else echo("Sin errores.")
                    report.diagnostics.forEach { d -> echo(DiagnosticStringFormatter.format(d)) }
                    if (report.hasErrors) throw ProgramResult(2)
                }
                is Failure -> {
                    val e = res.error
                    echo("Validate error (${e.stage}): ${e.message}")
                    e.cause?.let { echo("  causa: $it") }
                }
            }
        } catch (ioe: IOException) {
            echo("Error validando (IO): ${ioe.message}")
        } catch (iae: IllegalArgumentException) {
            echo("Error validando (argumentos): ${iae.message}")
        } finally {
            spinner.stop()
        }
    }

    private fun doValidate(version: Version) =
        Files.newBufferedReader(common.file).use { reader ->
            require(Files.exists(common.file)) { "No encuentro el archivo fuente: ${common.file.toAbsolutePath()}" }
            if (common.config != null) {
                require(Files.exists(common.config)) { "No encuentro el archivo de config: ${common.config!!.toAbsolutePath()}" }
            }

            val io = ProgramIo(reader = reader, configPath = common.config)
            Runner.validate(version, io)
        }
}
