import expr.ExpressionParser
import head.Head
import head.HeadDetector
import stmt.StmtParser

object Statements {
    @Suppress("LongParameterList")
    fun parseUntil(
        ts0: TokenStream,
        headDetector: HeadDetector,
        stmtParsers: Map<Head, StmtParser>,
        expr: ExpressionParser,
        isTerminator: (Any) -> Boolean,
        stopAtEof: Boolean = true,
    ): Result<Pair<List<Statement>, TokenStream>, LabeledError> {
        fun loop(
            ts: TokenStream,
            acc: List<Statement>,
        ): Result<Pair<List<Statement>, TokenStream>, LabeledError> =
            when {
                stopAtEof && ts.isEof() ->
                    Success(acc to ts)

                else ->
                    ts.peek().flatMap { tok ->
                        if (isTerminator(tok)) {
                            Success(acc to ts) // no consumimos el terminador
                        } else {
                            headDetector.detect(ts).flatMap { head ->
                                val parser = stmtParsers[head]
                                if (parser == null) {
                                    Failure(LabeledError.of(tok.span, "Inicio de sentencia no reconocido"))
                                } else {
                                    parser.parse(ts, expr).flatMap { (st, rest) ->
                                        loop(rest, acc + st)
                                    }
                                }
                            }
                        }
                    }
            }
        return loop(ts0, emptyList())
    }
}
