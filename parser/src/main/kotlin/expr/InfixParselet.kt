package expr

import Expression
import LabeledError
import Result
import TokenStream

interface InfixParselet {
    val prec: Prec
    fun parse(p: ExprPratt, left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
