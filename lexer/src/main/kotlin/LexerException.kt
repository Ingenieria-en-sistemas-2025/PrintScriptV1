class LexerException(message: String, val span: Span): RuntimeException(message) {
}