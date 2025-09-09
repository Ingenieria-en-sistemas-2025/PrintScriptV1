package expr

import LiteralNumber
import NumberLiteralToken
import TokenStream

object NumberPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ts.next().map { (tok, next) ->
            val t = tok as NumberLiteralToken
            LiteralNumber(t.raw, t.span) to next
        }
}
