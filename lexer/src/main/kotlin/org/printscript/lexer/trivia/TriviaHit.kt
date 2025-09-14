package org.printscript.lexer.trivia

data class TriviaHit(
    val length: Int,
    val kind: TriviaKind,
    val raw: String,
)
