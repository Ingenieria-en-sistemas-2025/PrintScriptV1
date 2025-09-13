package org.printscript.parser

import org.printscript.ast.StatementStream
import org.printscript.parser.expr.ExpressionParser
import org.printscript.parser.head.Head
import org.printscript.parser.head.HeadDetector
import org.printscript.parser.stmt.StmtParser
import org.printscript.token.TokenStream

class FirstParser(
    private val headDetector: HeadDetector,
    private val expr: ExpressionParser,
    private val stmtParsers: Map<Head, StmtParser>,
) : Parser {

    override fun parse(tokenStream: TokenStream): StatementStream {
        val parseOne = ParseOne.make(headDetector, stmtParsers, expr)
        return StreamingStatementStream.of(
            ts = tokenStream,
            parseOne = parseOne,
            headDetector = headDetector,
        )
    }
}
