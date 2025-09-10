package expr
import Expression
import LabeledError
import Result
import TokenStream

interface PrefixParselet {
    fun parse(p: ExpressionParser, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
