class SeparatorRule(table: Map<String, Separator>): LexingRule {
    private val map: Map<String, Separator> = table.toMap()

    override fun matchLength(string: String): Int {
        var best = 0
        for (sep in map.keys) {
            if (string.startsWith(sep) && sep.length > best) {
                best = sep.length
            }
        }
        return best
    }

    override fun build(lexeme: String, span: Span): Token =
        SeparatorToken(map.getValue(lexeme), span)

}