package expr

import BooleanLiteralToken
import LiteralBoolean
import TokenStream

object BooleanPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as BooleanLiteralToken
            LiteralBoolean(tok.value, tok.span) to next
        }
}
