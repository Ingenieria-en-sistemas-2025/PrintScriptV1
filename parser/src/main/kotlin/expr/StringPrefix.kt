package expr

import LiteralString
import StringLiteralToken
import TokenStream

object StringPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as StringLiteralToken
            LiteralString(tok.literal, tok.span) to next
        }
}
