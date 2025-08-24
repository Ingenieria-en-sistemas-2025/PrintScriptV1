object StringRule : LexingRule {
    override fun matchLength(string: String): Int {
        if (string.isEmpty()) return 0
        if (!startsWithSingleOrDoubleQuotes(string)) return 0
        val quote = string[0]

        var i = 1
        while (i < string.length) {
            val c = string[i]
            if (c == '\\') { // escapado
                i += 2
                continue
            }
            if (c == quote) return i + 1
            i++
        }

        return 0
    }

    override fun build(lexeme: String, span: Span): Token {
        val content = lexeme.substring(1, lexeme.length - 1)
        return StringLiteralToken(content, span)
    }

    private fun startsWithSingleOrDoubleQuotes(s: String): Boolean {
        val first = s[0]
        return first == '"' || first == '\'' // Ver despues q no este atado a esto, pasarselo o algo
    }
}
