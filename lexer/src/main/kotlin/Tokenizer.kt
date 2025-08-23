class Tokenizer(
    src: String,
    rules: List<LexingRule>,
    trivia: List<TriviaRule>,
) {

    private val init = Scanner(src)
    private val rules: List<LexingRule> = rules.toList() // copia de solo lectura
    private val trivia: List<TriviaRule> = trivia.toList()

    // Nucleo atomico -> Consume uno solo (lo pienso desp por tema memoria, habria que ver como usarlo sin acoplamiento)
    private fun lexOne(scanner: Scanner): Result<Pair<Token, Scanner>, LexerError> {
        return consumeAllTrivia(scanner).fold(
            onSuccess = { scAfterTrivia ->
                if (scAfterTrivia.eof()) {
                    Success(eof(scAfterTrivia) to scAfterTrivia)
                } else {
                    matchToken(scAfterTrivia)
                }
            },
            onFailure = { Failure(it) },
        )
    }

    fun tokenize(): Result<List<Token>, LexerError> {
        val out = ArrayList<Token>()
        var cur = init
        while (true) {
            when (val step = lexOne(cur)) {
                is Success -> {
                    val (tok, next) = step.value
                    out.add(tok)
                    cur = next
                    if (tok is EofToken) return Success(out)
                }
                is Failure -> return step
            }
        }
    }

    private fun nextTriviaAdvance(sc: Scanner): Result<Int, LexerError> {
        for (tr in trivia) {
            when (val r = tr.matchLen(sc)) {
                is Success -> if (r.value > 0) return Success(r.value)
                is Failure -> return r
            }
        }
        return Success(0)
    }

    private fun consumeAllTrivia(scanner: Scanner): Result<Scanner, LexerError> {
        var sc = scanner
        while (!sc.eof()) {
            when (val step = nextTriviaAdvance(sc)) {
                is Failure -> return step
                is Success -> if (step.value == 0) return Success(sc) else sc = sc.advance(step.value)
            }
        }
        return Success(sc)
    }

    private fun matchToken(sc: Scanner): Result<Pair<Token, Scanner>, LexerError> {
        val rem = sc.remaining()
        val start = sc.pos()
        val (rule, len) = chooseBestMatch(rem)

        if (len > 0) {
            val lex = rem.substring(0, len)
            val next = sc.advance(len)
            val tok = rule!!.build(lex, Span(start, next.pos()))
            return Success(tok to next)
        }

        // Nada matcheó -> error con span real de 1 char
        val next = sc.advance(1)
        return Failure((UnexpectedChar(Span(start, next.pos()), rem[0])))
    }

    // Eof helper
    private fun eof(sc: Scanner): Token {
        val p = sc.pos()
        return EofToken(Span(p, p))
    }

    // Longest match puro
    private fun chooseBestMatch(rem: String): BestMatch {
        var bestLen = 0
        var bestRule: LexingRule? = null
        for (r in rules) {
            val len = r.matchLength(rem)
            if (len > bestLen) {
                bestLen = len
                bestRule = r
            }
        }
        return BestMatch(bestRule, bestLen)
    }

    private data class BestMatch(val rule: LexingRule?, val len: Int)
}
