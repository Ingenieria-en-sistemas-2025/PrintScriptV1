object BlockCommentRule : TriviaRule {
    override fun matchLen(scanner: Scanner): Result<Int, LexerError> {
        val rem = scanner.remaining()
        if (!rem.startsWith("/*")) return Success(0)

        var i = 2
        while (i < rem.length - 1) {
            if (rem[i] == '*' && rem[i + 1] == '/') {
                return Success(i + 2) // consumimos "/*...*/"
            }
            i++
        }
        // No se encontró cierre → span desde inicio hasta EOF
        val startPos = scanner.pos()
        val endPos   = scanner.advance(rem.length).pos()
        return Failure(UnterminatedCommentBlock(Span(startPos, endPos))
        )

    }

}