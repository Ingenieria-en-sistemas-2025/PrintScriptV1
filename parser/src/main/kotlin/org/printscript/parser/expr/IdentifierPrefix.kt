package org.printscript.parser.expr

import org.printscript.ast.Variable
import org.printscript.token.IdentifierToken
import org.printscript.token.TokenStream

object IdentifierPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ts.next().map { (tok, next) ->
            val t = tok as IdentifierToken
            Variable(t.identifier, t.span) to next
        }
}
