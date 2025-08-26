data class RuntimeError(val span: Span?, override val message: String) :
    RuntimeException(message)
