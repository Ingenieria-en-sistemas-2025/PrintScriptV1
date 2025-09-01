interface Formatter {
    fun format(ts: TokenStream): Result<String, LabeledError>
}
