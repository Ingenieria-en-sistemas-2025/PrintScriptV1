package org.printscript.token

import org.printscript.common.Separator
import org.printscript.common.Span

data class SeparatorToken(val separator: Separator, override val span: Span) : Token {
    override fun toString() = "SEP($separator)"
}
