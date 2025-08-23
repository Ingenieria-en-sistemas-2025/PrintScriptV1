object LineCommentRule : TriviaRule {
    override fun matchLen(scanner : Scanner): Result<Int, LexerError> {
        val rem = scanner.remaining()
        if (!rem.startsWith("//")) return Success(0)
        var i = 2
        while (i < rem.length && rem[i] != '\n') i++
        //no consumimos el '\n' (queda para la siguiente pasada)
        return Success(i) // incluye hasta justo antes del '\n'
    }
}