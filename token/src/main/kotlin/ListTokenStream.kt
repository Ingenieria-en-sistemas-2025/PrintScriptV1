class ListTokenStream private constructor( private val list: List<Token>,
                                           private val index: Int): TokenStream {

    companion object {
        fun of(tokens: List<Token>): ListTokenStream {
            require(tokens.isNotEmpty()) { "Se esperaba al menos EOF." }
            require(tokens.last() is EofToken) {
                "La lista debe terminar en EofToken; último=${tokens.last()::class.simpleName}"
            }
            return ListTokenStream(tokens, 0)
        }
    }

    override fun peek(): Result<Token, LabeledError> =
        Success(list[index])

    override fun next(): Result<Pair<Token, TokenStream>, LabeledError> {
        val tok = list[index]
        val nextIdx = if (index < list.lastIndex) index + 1 else index
        val nextStream = ListTokenStream(list, nextIdx)
        return Success(tok to nextStream)
    }

    override fun isEof(): Boolean = list[index] is EofToken
}