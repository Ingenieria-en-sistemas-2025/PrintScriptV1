package org.printscript.formatter

import org.printscript.token.Token

interface LayoutApplier {
    data class State(val level: Int = 0)
    fun applyPrefix(
        prefix: String?,
        prev: Token?,
        current: Token,
        next: Token?,
        state: State,
    ): Pair<List<String>, State>

    fun updateAfter(prev: Token?, current: Token, state: State): State
    fun spacing(state: State): String
}
