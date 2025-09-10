package expr

import Expression
import LabeledError
import Result
import TokenStream

interface InfixParselet {
    val prec: Prec
    fun parse(p: ExpressionParser, left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
