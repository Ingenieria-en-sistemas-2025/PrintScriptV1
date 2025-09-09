package org.printscript.parser

import org.printscript.ast.ProgramNode
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.parser.expr.ExpressionParser
import org.printscript.parser.expr.RecursiveExpressionParser
import org.printscript.parser.head.FirstHeadDetector
import org.printscript.parser.head.Head
import org.printscript.parser.head.HeadDetector
import org.printscript.parser.stmt.StmtParser
import org.printscript.token.TokenStream

class FirstParser(
    private val headDetector: HeadDetector = FirstHeadDetector(),
    private val expr: ExpressionParser = RecursiveExpressionParser(),
    private val stmtParsers: Map<Head, StmtParser>,
) : Parser {

    override fun parse(ts0: TokenStream): Result<ProgramNode, LabeledError> =
        Statements.parseUntil(
            ts0,
            headDetector,
            stmtParsers,
            expr,
            isTerminator = { false }, // nunca cortamos por token
            stopAtEof = true, // cortamos por EOF
        ).map { (stmts, _) -> ProgramNode(stmts) }
}
