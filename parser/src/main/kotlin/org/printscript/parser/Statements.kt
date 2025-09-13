package org.printscript.parser

import org.printscript.ast.Statement
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.parser.expr.ExpressionParser
import org.printscript.parser.head.Head
import org.printscript.parser.head.HeadDetector
import org.printscript.parser.stmt.StmtParser
import org.printscript.token.Token
import org.printscript.token.TokenStream

object Statements {
    fun parseUntil(
        ts0: TokenStream,
        headDetector: HeadDetector,
        stmtParsers: Map<Head, StmtParser>,
        expr: ExpressionParser,
        isTerminator: (Token) -> Boolean,
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
