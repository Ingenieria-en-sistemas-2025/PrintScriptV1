internal class Scanner private constructor(private val input: String, private val index: Int, private val line: Int, private val column: Int) {
    constructor(scr: String): this(scr, 0, 1, 1)

    fun eof(): Boolean = index >= input.length

    fun peek(): Char {
        if (eof()) error("EOF")
        return input[index]
    }

    fun advance(n: Int): Scanner {
        require(n >= 0) { "n must be non-negative" }
        var i = index
        var l = line
        var c = column
        val end = (index + n).coerceAtMost(input.length)
        while (i < end) {
            val ch = input[i++]
            if (ch == '\n') { l++; c = 1 } else { c++ }
        }
        return Scanner(input, i, l, c)
    }

    fun pos(): Position = Position(line, column)
    fun remaining(): String = if (eof()) "" else input.substring(index)
    fun spanFrom(start: Position): Span = Span(start, pos())

}