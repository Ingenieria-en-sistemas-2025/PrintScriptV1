package expr

import LiteralString
import StringLiteralToken
import TokenStream

object StringPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ts.next().map { (tok, next) ->
            val t = tok as StringLiteralToken
            LiteralString(t.literal, t.span) to next
        }
}
