package org.printscript.formatter

import org.printscript.common.Separator
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class IndentationApplier(private val indentSize: Int) : LayoutApplier {

    override fun applyPrefix(
        prefix: String?, // " ", "\n"
        prev: Token?,
        current: Token,
        next: Token?,
        state: LayoutApplier.State,
    ): Pair<List<String>, LayoutApplier.State> {
        if (prefix == null) return emptyList<String>() to state
        if (!prefix.startsWith("\n")) return listOf(prefix) to state

        val nl = prefix.count { it == '\n' }
        val level = computeLevel(prev, current, next, state)
        val chunk = buildString {
            repeat(nl) { append('\n') }
            if (level > 0) repeat(level * indentSize) { append(' ') }
        }
        return listOf(chunk) to state
    }

    override fun updateAfter(prev: Token?, current: Token, state: LayoutApplier.State): LayoutApplier.State = when {
        prev.isLbrace() -> state.copy(level = state.level + 1)
        current.isRbrace() -> state.copy(level = (state.level - 1).coerceAtLeast(0))
        else -> state
    }

    private fun computeLevel(prev: Token?, current: Token, next: Token?, s: LayoutApplier.State): Int = when {
        prev.isLbrace() -> s.level + 1
        current.isRbrace() -> (s.level - 1).coerceAtLeast(0)
        prev.isSemicolon() -> if (next.isRbrace()) (s.level - 1).coerceAtLeast(0) else s.level
        else -> s.level
    }
}

private fun Token?.isSeparator(kind: Separator) =
    this is SeparatorToken && this.separator == kind
private fun Token?.isLbrace() = this.isSeparator(Separator.LBRACE)
private fun Token?.isRbrace() = this.isSeparator(Separator.RBRACE)
private fun Token?.isSemicolon() = this.isSeparator(Separator.SEMICOLON)
