package org.printscript.lexer

data class LexingConfig(
    val rules: List<LexingRule>,
    val trivia: List<TriviaRule>,
)
