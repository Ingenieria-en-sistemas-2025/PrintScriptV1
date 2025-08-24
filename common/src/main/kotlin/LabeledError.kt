interface LabeledError {
    val span: Span
    val message: String
    fun humanReadable(): String = "$message @ ${span.start.line}:${span.start.column}"
}
