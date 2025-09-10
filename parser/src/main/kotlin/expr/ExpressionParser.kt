package expr

import Expression
import LabeledError
import Result
import TokenStream

interface ExpressionParser {
    fun parseExpression(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
    fun parseWith(ts: TokenStream, minPrec: Prec): Result<Pair<Expression, TokenStream>, LabeledError>
}
