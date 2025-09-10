package org.printscript.lexer

import org.printscript.common.Position

class Scanner private constructor(
    private val input: String,
    private val index: Int,
    private val line: Int,
    private val column: Int,
) {
    constructor(text: String) : this(text, 0, 1, 1)

    val isEof: Boolean get() = index >= input.length

    fun peek(): Char = input.getOrNull(index) ?: error("EOF")

    fun remainingFrom(offset: Int = 0): CharSequence = input.substring(index + offset, input.length)

    fun position(): Position = Position(line, column)

    fun advance(n: Int): Scanner {
        require(n >= 0) { "n must be non-negative" }
        var i = index
        var l = line
        var c = column
        val end = (index + n).coerceAtMost(input.length)
        while (i < end) {
            val ch = input[i++]
            if (ch == '\n') {
                l++
                c = 1
            } else { c++ }
        }
        return Scanner(input, i, l, c)
    }

    fun slice(len: Int): CharSequence =
        input.subSequence(index, (index + len).coerceAtMost(input.length))
}
