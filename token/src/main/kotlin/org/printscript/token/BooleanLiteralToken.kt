package org.printscript.token

import org.printscript.common.Span

data class BooleanLiteralToken(val value: Boolean, override val span: Span) : Token {
    override fun toString() = "BOOL($value)"
}
