package org.printscript.token

import org.printscript.common.Span

data class EofToken(override val span: Span) : Token {
    override fun toString() = "EOF"
}
