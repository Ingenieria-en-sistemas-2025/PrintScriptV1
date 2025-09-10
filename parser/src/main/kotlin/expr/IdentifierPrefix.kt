package expr

import IdentifierToken
import TokenStream
import Variable

object IdentifierPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (tok, next) ->
            val t = tok as IdentifierToken
            Variable(t.identifier, t.span) to next
        }
}
