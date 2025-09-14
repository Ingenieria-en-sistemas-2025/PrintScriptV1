package org.printscript.lexer.lexingrules

object StringRule : LexingRule {
    override val key: RuleKey = RuleKey("STRING")

    private const val ESCAPE = '\\'
    private const val QUOTE_DOUBLE = '"'
    private const val QUOTE_SINGLE = '\''

    override fun matchLength(input: CharSequence): Int {
        val opening = charAt(input, 0) ?: return 0
        if (!isQuote(opening)) return 0

        var i = 1
        while (true) {
            val c = charAt(input, i)
            when {
                c == null -> {
                    // No alcanzó el cierre en este slice → “hit boundary” para que el matcher expanda el probe.
                    return input.length
                }
                c == ESCAPE -> {
                    // saltar el escapado si existe; si no, no hay match (unterminated)
                    if (charAt(input, i + 1) == null) return input.length
                    i += 2
                }
                c == '\n' || c == '\r' -> return 0 // opcional: no string multilínea
                c == opening -> return i + 1 // cierre encontrado (incluí comillas)
                else -> i++
            }
        }
    }

    private fun isQuote(c: Char) = c == QUOTE_DOUBLE || c == QUOTE_SINGLE

    private fun charAt(s: CharSequence, index: Int): Char? =
        try { s[index] } catch (_: IndexOutOfBoundsException) { null }
}
