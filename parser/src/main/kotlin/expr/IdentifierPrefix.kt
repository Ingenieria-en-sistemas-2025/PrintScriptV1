package expr

import IdentifierToken
import TokenStream
import Variable

object IdentifierPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as IdentifierToken
            Variable(tok.identifier, tok.span) to next
        }
}
