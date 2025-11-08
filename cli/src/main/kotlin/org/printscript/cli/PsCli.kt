package org.printscript.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import org.printscript.cli.commands.AnalyzeCmd
import org.printscript.cli.commands.ExecuteCmd
import org.printscript.cli.commands.FormatCmd
import org.printscript.cli.commands.ValidateCmd

fun main(args: Array<String>) {
    PrintScriptCli() // comando raiz, el programa
        .subcommands(
            ValidateCmd(),
            ExecuteCmd(),
            FormatCmd(),
            AnalyzeCmd(),
        )
        .main(args) // le entrega a Clikt los args de la consola
    // Clikt los parsea decide q subcomando corre y maneja errores y eso
}

class PrintScriptCli : CliktCommand(
    name = "printscript",
    help = "PrintScript CLI - A command-line interface for PrintScript language operations",
) {
    override fun run() = Unit
}
