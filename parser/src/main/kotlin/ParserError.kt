class ParserError(
    override val span: Span,
    override val message: String,
) : LabeledError
