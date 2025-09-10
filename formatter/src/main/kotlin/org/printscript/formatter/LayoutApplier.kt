package org.printscript.formatter

import org.printscript.token.Token

interface LayoutApplier {
    data class State(val level: Int = 0)

    // Retorna los chunks a emitir por prefijo + el nuevo estado (si corresponde)
    fun applyPrefix(
        prefix: String?,
        prev: Token?,
        current: Token,
        next: Token?,
        state: State,
    ): Pair<List<String>, State>

    // Ajusta el estado despues de emitir el token actual (abre/cierra bloque)
    fun updateAfter(prev: Token?, current: Token, state: State): State
}
