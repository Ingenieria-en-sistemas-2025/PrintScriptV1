package org.printscript.formatter

import org.printscript.common.Separator
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class IndentationApplier(private val indentSize: Int) : LayoutApplier {

    override fun applyPrefix(
        prefix: String?, // " ", "\n", "\n\n", etc.
        prev: Token?,
        current: Token,
        next: Token?,
        state: LayoutApplier.State,
    ): Pair<List<String>, LayoutApplier.State> {
        var usedPrefix = ""
        if (prefix != null) usedPrefix = prefix

        if (!usedPrefix.startsWith("\n")) {
            return listOf(usedPrefix) to state
        }

        val nl = usedPrefix.count { it == '\n' }

        val level = when {
            prev is SeparatorToken && prev.separator == Separator.RBRACE ->
                (state.level - 1).coerceAtLeast(0)
            else ->
                computeLevel(prev, current, next, state)
        }

        val chunks = mutableListOf<String>()
        repeat(nl) { chunks += "\n" }
        if (level > 0) chunks += " ".repeat(level * indentSize)
        return chunks to state
    }

    override fun updateAfter(prev: Token?, current: Token, state: LayoutApplier.State): LayoutApplier.State = when {
        prev.isLbrace() -> state.copy(level = state.level + 1)
        current.isRbrace() -> state.copy(level = (state.level - 1).coerceAtLeast(0))
        else -> state
    }

    override fun spacing(state: LayoutApplier.State): String {
        if (state.level > 0) return " ".repeat((state.level * indentSize) - 1)
        return ""
    }

    private fun computeLevel(prev: Token?, current: Token, next: Token?, s: LayoutApplier.State): Int {
        val result = when {
            prev.isLbrace() -> s.level + 1
            current.isRbrace() -> (s.level - 1).coerceAtLeast(0)
            prev.isSemicolon() -> if (next.isRbrace()) (s.level - 1).coerceAtLeast(0) else s.level
            else -> s.level
        }

        return result
    }

    private fun Token?.isSeparator(kind: Separator) =
        this is SeparatorToken && this.separator == kind

    private fun Token?.isLbrace() = this.isSeparator(Separator.LBRACE)
    private fun Token?.isRbrace() = this.isSeparator(Separator.RBRACE)
    private fun Token?.isSemicolon() = this.isSeparator(Separator.SEMICOLON)
}
