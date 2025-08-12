import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val parser = ArgParser("printscript")

    val input by parser.option(
        ArgType.String, shortName = "i",
        description = "Archivo de entrada"
    ).default("")

    val mode by parser.option(
        ArgType.String, shortName = "m",
        description = "Etapa a ejecutar: lex | parse | eval"
    ).default("parse")

    parser.parse(args)

    val allowed = setOf("lex", "parse", "eval")
    require(mode in allowed) {
        "Modo inválido: '$mode'. Use uno de: ${allowed.joinToString(", ")}"
    }

    log.info { "Iniciando PrintScriptV1 (mode=$mode, input=$input)" }

    when (mode) {
        "lex" -> println("Ejecutando lexer sobre $input")
        "parse" -> println("Ejecutando parser sobre $input")
        "eval" -> println("Ejecutando intérprete sobre $input")
    }
}