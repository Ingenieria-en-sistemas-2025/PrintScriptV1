package stmt

import Statement
import TokenStream
import expr.ExpressionParser

interface StmtParser {
    fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement
}
