package org.printscript.token

import org.printscript.common.Span

sealed interface TriviaToken : Token

data class WhitespaceToken(val raw: String, override val span: Span) : TriviaToken {
    override fun toString() = "WS(${raw.length})"
}
data class NewlineToken(override val span: Span) : TriviaToken {
    override fun toString() = "NL"
}
data class LineCommentToken(val raw: String, override val span: Span) : TriviaToken {
    override fun toString() = "LCOMMENT"
}
data class BlockCommentToken(val raw: String, override val span: Span) : TriviaToken {
    override fun toString() = "BCOMMENT"
}
