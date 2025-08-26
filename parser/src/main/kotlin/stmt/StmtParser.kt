package stmt

import LabeledError
import Result
import Statement
import TokenStream
import expr.ExpressionParser

interface StmtParser {
    fun parse(ts: TokenStream, expressionParser: ExpressionParser): Result<Pair<Statement, TokenStream>, LabeledError>
}
