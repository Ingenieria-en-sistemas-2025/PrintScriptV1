interface LexingRule {

    fun matchLength(string: String): Int
    fun build(lexeme: String, span: Span): Token

}