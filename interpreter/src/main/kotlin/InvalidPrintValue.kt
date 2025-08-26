class InvalidPrintValue(override val span: Span) : InterpreterError {
    override val message = "Valor inválido para imprimir"
}
