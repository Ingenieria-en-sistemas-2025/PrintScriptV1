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
import org.printscript.token.EofToken
import org.printscript.token.Token
import org.printscript.token.TokenStream

object ParseOne {
    fun make(
        headDetector: HeadDetector,
        stmtParsers: Map<Head, StmtParser>,
        expr: ExpressionParser,
    ): (TokenStream) -> Result<Pair<Statement, TokenStream>, LabeledError> = { ts ->
        if (ts.isEof()) {
            val peek = ts.peek()
            when (peek) {
                is Success -> {
                    val eofTok = peek.value as EofToken
                    Failure(LabeledError.of(eofTok.span, "EOF inesperado"))
                }
                is Failure -> peek
            }
        } else {
            headDetector.detect(ts).flatMap { head ->
                val stmtp = stmtParsers[head]
                if (stmtp == null) {
                    ts.peek().fold(
                        onSuccess = { tok: Token ->
                            Failure(LabeledError.of(tok.span, "Inicio de sentencia no reconocido"))
                        },
                        onFailure = { err -> Failure(err) },
                    )
                } else {
                    stmtp.parse(ts, expr) // -> Result<Pair<Statement, TokenStream>, LabeledError>
                }
            }
        }
    }
}
