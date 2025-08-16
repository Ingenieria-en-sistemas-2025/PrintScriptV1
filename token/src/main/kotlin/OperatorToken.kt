data class OperatorToken(val operator: Operator, override val span: Span): Token{
    override fun toString() = "OP($operator)"
}
