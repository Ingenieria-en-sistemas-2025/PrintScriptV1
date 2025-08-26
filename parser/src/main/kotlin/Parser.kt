interface Parser {
    fun parse(tokenStream: TokenStream): Result<ProgramNode, LabeledError>
}
