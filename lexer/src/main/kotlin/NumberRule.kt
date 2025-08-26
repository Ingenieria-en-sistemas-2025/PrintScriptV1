
private val NUM_REGEX = Regex("\\d+(?:\\.\\d+)?")

class NumberRule : LexingRule {
    override fun matchLength(string: String): Int {
        val match = NUM_REGEX.matchAt(string, 0) ?: return 0
        return match.value.length
    }

    override fun build(lexeme: String, span: Span): Token =
        NumberLiteralToken(lexeme, span)
}
