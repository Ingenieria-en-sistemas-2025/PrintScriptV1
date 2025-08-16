class OperatorRule(map: Map<String, Operator>): LexingRule {

    private val table: Map<String, Operator> = map.toMap()

    override fun matchLength(string: String): Int {
        var best = 0
        for (op in table.keys){
            if (string.startsWith(op) && op.length > best) {
                best = op.length
            }
        }
        return best
    }

    override fun build(lexeme: String, span: Span): Token {
        OperatorToken(table.getValue(lexeme), span) // Ojo con esto por si no matchea, manejar casos
    }
}