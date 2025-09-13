package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.lexer.memory.CharFeed
import org.printscript.lexer.memory.LineIteratorFeed
import java.io.Reader

class Scanner private constructor(
    private val source: Source,
    private val absIndex: Int,
    private val line: Int,
    private val column: Int,
) {

    constructor(text: String) : this(StringSource(text), 0, 1, 1)
    constructor(reader: Reader) : this(
        FeedSource(LineIteratorFeed(reader.buffered().lines().iterator())),
        0,
        1,
        1,
    )
    constructor(lines: Iterator<String>) : this(
        FeedSource(LineIteratorFeed(lines)),
        0,
        1,
        1,
    )
    constructor(feed: CharFeed) : this(FeedSource(feed), 0, 1, 1)

    val isEof: Boolean get() = source.isEof(absIndex)
    fun position(): Position = Position(line, column)
    fun peek(): Char = source.charAt(absIndex) ?: error("EOF")

    fun remainingFrom(offset: Int = 0): CharSequence = source.rollingSlice(absIndex + offset)

    fun slice(len: Int): CharSequence = source.fixedSlice(absIndex, len)

    fun advance(n: Int): Scanner {
        require(n >= 0) { "n must be non-negative" }
        var idx = absIndex
        var l = line
        var c = column
        var moved = 0
        while (moved < n) {
            val ch = source.charAt(idx) ?: break // EOF suave
            idx++
            if (ch == '\n') {
                l++
                c = 1
            } else { c++ }
            moved++
        }
        return Scanner(source, idx, l, c)
    }

    fun pinHere() { source.pin(absIndex) }
    fun unpinHere() { source.unpin(absIndex) }

    private interface Source {
        fun isEof(absIndex: Int): Boolean
        fun charAt(absIndex: Int): Char?
        fun rollingSlice(absIndex: Int): CharSequence
        fun fixedSlice(absIndex: Int, len: Int): CharSequence
        fun pin(absIndex: Int) {} // default no-op
        fun unpin(absIndex: Int) {} // default no-op
    }

    private class StringSource(private val text: String) : Source {
        override fun isEof(absIndex: Int) = absIndex >= text.length
        override fun charAt(absIndex: Int) =
            if (absIndex in 0 until text.length) text[absIndex] else null
        override fun rollingSlice(absIndex: Int) =
            if (absIndex >= text.length) "" else text.subSequence(absIndex, text.length)
        override fun fixedSlice(absIndex: Int, len: Int): CharSequence {
            val end = (absIndex + len).coerceAtMost(text.length)
            return text.subSequence(absIndex, end)
        }
    }

    private class FeedSource(private val feed: CharFeed) : Source {
        override fun isEof(absIndex: Int) = feed.eofFrom(absIndex)
        override fun charAt(absIndex: Int) = feed.charAt(absIndex)
        override fun rollingSlice(absIndex: Int) = feed.rollingSlice(absIndex)
        override fun fixedSlice(absIndex: Int, len: Int) = feed.fixedSlice(absIndex, len)

        override fun pin(absIndex: Int) = feed.pin(absIndex)
        override fun unpin(absIndex: Int) = feed.unpin(absIndex)
    }
}
