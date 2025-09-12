package org.printscript.lexer

import org.printscript.common.Span

data class Lexeme(val text: String, val span: Span)
