class ParseError(val span: Span, message: String) : RuntimeException(message)
