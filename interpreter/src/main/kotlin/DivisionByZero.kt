data class DivisionByZero(override val span: Span) : InterpreterError {
    override val message: String = "Error: division by zero"
}
