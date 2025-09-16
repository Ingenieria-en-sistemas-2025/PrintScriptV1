package org.printscript.lexer.memory

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Tokenizer
import org.printscript.token.EofToken
import org.printscript.token.Token
import org.printscript.token.TokenStream

class BufferedStreamingTokenStream private constructor(
    private val shared: SharedState,
    private val logicalIndex: Int,
) : TokenStream {

    private class SharedState(
        var tokenizer: Tokenizer,
        val buffer: ArrayList<Token> = arrayListOf(),
        val maxRetained: Int = 4096,
        var baseOffset: Int = 0, // cuantos tokens descartamos del frente
    )

    companion object {
        fun of(tokenizer: Tokenizer): TokenStream =
            BufferedStreamingTokenStream(SharedState(tokenizer), 0)
    }

    private fun ensure(n: Int): Result<Unit, LabeledError> {
        val requiredAbs = logicalIndex + n
        while (shared.baseOffset + shared.buffer.size <= requiredAbs) {
            val last = shared.buffer.lastOrNull()
            if (last is EofToken) return Success(Unit)

            when (val step = shared.tokenizer.next()) {
                is Failure -> return Failure(step.error)
                is Success -> {
                    val (producedToken, nextTokenizer) = step.value
                    shared.buffer.add(producedToken)
                    shared.tokenizer = nextTokenizer
                    if (producedToken is EofToken) return Success(Unit)
                }
            }
        }

        maybeCompact()
        return Success(Unit)
    }

    private fun bufferIndexFor(absLogical: Int): Int =
        absLogical - shared.baseOffset

    private fun maybeCompact() {
        val retainedAhead = logicalIndex - shared.baseOffset // cuántos ya “pasamos” pero siguen retenidos
        val tooBig = shared.buffer.size > shared.maxRetained * 2
        val farAhead = retainedAhead > shared.maxRetained

        if (tooBig && farAhead) {
            val dropCount = retainedAhead - shared.maxRetained
            shared.buffer.subList(0, dropCount).clear()
            shared.baseOffset += dropCount
        }
    }

    override fun peek(n: Int): Result<Token, LabeledError> =
        when (val ready = ensure(n)) {
            is Failure -> ready
            is Success -> {
                val reqAbs = logicalIndex + n
                val idx = bufferIndexFor(reqAbs).coerceAtMost(shared.buffer.lastIndex)
                Success(shared.buffer[idx])
            }
        }

    override fun next(): Result<Pair<Token, TokenStream>, LabeledError> =
        when (val ready = ensure(0)) {
            is Failure -> ready
            is Success -> {
                val idx = bufferIndexFor(logicalIndex)
                val current = shared.buffer[idx]
                val advanced = BufferedStreamingTokenStream(shared, logicalIndex + 1)
                Success(current to advanced)
            }
        }

    override fun isEof(): Boolean =
        when (ensure(0)) {
            is Failure -> true
            is Success -> {
                val last = shared.buffer.lastOrNull()
                if (last is EofToken) {
                    val eofAbsIndex = shared.baseOffset + shared.buffer.lastIndex
                    logicalIndex >= eofAbsIndex
                } else {
                    false
                }
            }
        }
}
