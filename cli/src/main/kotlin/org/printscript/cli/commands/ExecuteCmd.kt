package org.printscript.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.printscript.cli.CliSupport
import org.printscript.cli.CommonOptions
import org.printscript.cli.ProgressSpinner
import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.ProgramIo
import org.printscript.runner.runners.ExecuteRunnerStreaming
import java.io.IOException
import java.nio.file.Files

class ExecuteCmd : CliktCommand(
    name = "execute",
    help = "Interpreta y ejecuta un programa PrintScript",
) {
    private val common by CommonOptions()
    private val collect by option("--collect-also").flag(default = false)

    override fun run() {
        val version = CliSupport.resolveVersion(common.version)

        // Si hay TTY (modo interactivo), no muestres spinner
        val interactive = System.console() != null
        val spinner = if (interactive) null else ProgressSpinner("Ejecutando").also { it.start() }

        try {
            when (val rr = doExecute(version)) {
                is Success -> Unit
                is Failure -> {
                    val e = rr.error
                    echo("Execute error (${e.stage}): ${e.message}")
                    e.cause?.let { echo("  causa: $it") }
                }
            }
        } catch (ioe: IOException) {
            echo("Error ejecutando (IO): ${ioe.message}")
        } catch (iae: IllegalArgumentException) {
            echo("Error ejecutando (argumentos): ${iae.message}")
        } finally {
            spinner?.stop()
        }
    }

    private fun doExecute(version: Version) =
        Files.newBufferedReader(common.file).use { reader ->
            val io = ProgramIo(reader = reader)
            val runner = ExecuteRunnerStreaming(
                printer = { line ->
                    println(line)
                    System.out.flush()
                },
                collectAlsoWithPrinter = collect,
            )
            println()
            System.out.flush()
            runner.run(version, io)
        }
}
