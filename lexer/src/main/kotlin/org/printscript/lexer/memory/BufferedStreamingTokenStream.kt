package org.printscript.lexer.memory

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Tokenizer
import org.printscript.token.EofToken
import org.printscript.token.Token
import org.printscript.token.TokenStream

class BufferedStreamingTokenStream private constructor( // modularizar, expresividad.
    private val shared: SharedState,
    private val cursor: Int, // // índice lógico actual (absoluto en términos de tokens producidos)
) : TokenStream {

    private class SharedState(
        var tokenizer: Tokenizer, // productor de tokens (inmutable por step; lo vamos reemplazando).
        val tokens: ArrayList<Token> = arrayListOf(), // ventana de tokens retenidos.
        val maxRetainedTokens: Int = 4096, // límite "soft" de retención (se compacta cuando lo sobrepasamos mucho).
        var headOffset: Int = 0, // cuantos tokens descartamos del frente
    )

    companion object {
        fun of(tokenizer: Tokenizer): TokenStream =
            BufferedStreamingTokenStream(SharedState(tokenizer), 0)
    }

    private fun absSize(): Int = shared.headOffset + shared.tokens.size // próximo índice absoluto aún no producido
    private fun hasAbsIndex(absIndex: Int): Boolean = absIndex < absSize() // Ya tenemos en buffer el índice absoluto requerido??
    private fun relIndex(absIndex: Int): Int = absIndex - shared.headOffset

    private fun ensureCovers(requiredAbs: Int): Result<Unit, LabeledError> {
        while (!hasAbsIndex(requiredAbs)) {
            if (shared.tokens.lastOrNull() is EofToken) return Success(Unit)
            when (val step = shared.tokenizer.next()) {
                is Failure -> return Failure(step.error) // error léxico
                is Success -> {
                    val (produced, nextTok) = step.value
                    shared.tokens.add(produced)
                    shared.tokenizer = nextTok
                    if (produced is EofToken) break
                }
            }
        }
        maybeCompactBuffer()
        return Success(Unit)
    }

    private fun maybeCompactBuffer() {
        val consumed = cursor - shared.headOffset
        val bufferTooLarge = shared.tokens.size > shared.maxRetainedTokens * 2
        val farPast = consumed > shared.maxRetainedTokens

        if (bufferTooLarge && farPast) {
            val dropCount = consumed - shared.maxRetainedTokens
            if (dropCount > 0) {
                shared.tokens.subList(0, dropCount).clear()
                shared.headOffset += dropCount
            }
        }
    }

    override fun peek(n: Int): Result<Token, LabeledError> =
        when (val r = ensureCovers(cursor + n)) {
            is Failure -> r
            is Success -> {
                val requiredAbs = cursor + n
                val idx = relIndex(requiredAbs)
                val last = shared.tokens.lastOrNull()

                if (idx <= shared.tokens.lastIndex) {
                    Success(shared.tokens[idx])
                } else {
                    if (last is EofToken) {
                        Success(last)
                    } else {
                        error("Invariant breach: ensureCovers() Success pero no hay token y el último no es EOF")
                    }
                }
            }
        }

    override fun next(): Result<Pair<Token, TokenStream>, LabeledError> =
        when (val r = ensureCovers(cursor)) {
            is Failure -> r
            is Success -> {
                val idx = relIndex(cursor)
                val current = shared.tokens.getOrNull(idx)
                    ?: run {
                        val last = shared.tokens.lastOrNull()
                        require(last is EofToken) { "Invariant breach: sin token en cursor y el último no es EOF" }
                        last
                    }
                val advanced = BufferedStreamingTokenStream(shared, cursor + 1)
                Success(current to advanced)
            }
        }

    override fun isEof(): Boolean =
        when (ensureCovers(cursor)) {
            is Failure -> false
            is Success -> {
                val idx = relIndex(cursor)
                val cur = shared.tokens.getOrNull(idx)
                when {
                    cur is EofToken -> true
                    cur == null && (shared.tokens.lastOrNull() is EofToken) -> true
                    else -> false
                }
            }
        }
}
