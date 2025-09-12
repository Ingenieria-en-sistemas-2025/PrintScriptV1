package org.printscript.token

import org.printscript.common.Span

data class NumberLiteralToken(val raw: String, override val span: Span) : WordLikeToken {
    override fun toString() = "NUM($raw)"
}
