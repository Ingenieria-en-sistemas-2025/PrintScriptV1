package org.printscript.runner

sealed interface Stage {
    data object Lexing : Stage
    data object Parsing : Stage
    data object Analyzing : Stage
    data object Formatting : Stage
    data object Interpreting : Stage
}
