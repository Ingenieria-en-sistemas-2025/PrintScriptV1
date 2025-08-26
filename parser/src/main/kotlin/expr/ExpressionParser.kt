package expr

import Expression
import LabeledError
import Result
import TokenStream

interface ExpressionParser {
    fun parseExpression(tokenStream: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
