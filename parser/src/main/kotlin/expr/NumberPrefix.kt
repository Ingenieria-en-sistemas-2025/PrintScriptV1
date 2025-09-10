package expr

import LiteralNumber
import NumberLiteralToken
import TokenStream

object NumberPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as NumberLiteralToken
            LiteralNumber(tok.raw, tok.span) to next
        }
}
