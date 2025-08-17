interface Parser {
    fun parse(tokenStream: TokenStream): ProgramNode
}