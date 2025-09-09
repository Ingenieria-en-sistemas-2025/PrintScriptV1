package org.printscript.parser.expr

import org.printscript.ast.LiteralNumber
import org.printscript.token.NumberLiteralToken
import org.printscript.token.TokenStream

object NumberPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ts.next().map { (tok, next) ->
            val t = tok as NumberLiteralToken
            LiteralNumber(t.raw, t.span) to next
        }
}
