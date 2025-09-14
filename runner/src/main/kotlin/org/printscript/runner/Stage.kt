package org.printscript.runner

// marcan etapa por lo de los errores q dije en el audio
interface Stage
object Lexing : Stage
object Parsing : Stage
object Analyzing : Stage
object Formatting : Stage
object Interpreting : Stage
