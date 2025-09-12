package org.printscript.token

import org.printscript.common.Keyword
import org.printscript.common.Span

data class KeywordToken(val kind: Keyword, override val span: Span) : WordLikeToken {
    override fun toString() = "KW($kind)"
}
