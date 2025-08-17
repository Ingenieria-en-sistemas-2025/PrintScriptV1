class Tokenizer(src: String,
                rules: List<LexingRule>) {

    private val init = Scanner(src)
    private val rules: List<LexingRule> = rules.toList() // copia de solo lectura

    fun tokenize(): List<Token> {
        val out = ArrayList<Token>()
        var sc = init

        while (true) {
            sc = skipTrivial(sc)
            if (sc.eof()) {
                val p = sc.pos()
                out.add(EofToken(Span(p, p)))
                break
            }

            val rem = sc.remaining()
            val start = sc.pos()

            val (rule, len) = chooseBestMatch(rem)
            if (len > 0) {
                val lex = rem.substring(0, len)
                val sc2 = sc.advance(len)
                out.add(rule!!.build(lex, Span(start, sc2.pos())))
                sc = sc2
                continue
            }

            val bad = sc.peek()
            val sc2 = sc.advance(1)
            throw LexerException("Símbolo inesperado: '$bad'", Span(start, sc2.pos()))

        }
        return out.toList()
    }

    // Longest match sobre todas las reglas
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


    private fun skipTrivial(scanner: Scanner): Scanner {
        var sc = scanner
        while (!sc.eof()){
            val rem = sc.remaining()
            val c = rem[0]

            if (c.isWhitespace()) { sc = sc.advance(1); continue }

            // Comentario de linea
            if (rem.startsWith("//")) {
                var i = 2
                while (i < rem.length && rem[i] != '\n') i++
                sc = sc.advance(i)
                continue
            }

            // Comentario de bloque (no se si es necesario)
            if (rem.startsWith("/*")) {
                var i = 2
                while (i < rem.length - 1 && !(rem[i] == '*' && rem[i + 1] == '/')) i++
                if (i >= rem.length - 1) {
                    throw LexerException("Comentario no cerrado", Span(sc.pos(), sc.pos()))
                }
                sc = sc.advance(i + 2)
                continue
            }

            break
        }
        return sc
    }

    private data class BestMatch(val rule: LexingRule?, val len: Int)

}