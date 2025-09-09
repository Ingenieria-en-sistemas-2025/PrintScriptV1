package org.printscript.parser.expr

import org.printscript.ast.LiteralString
import org.printscript.token.StringLiteralToken
import org.printscript.token.TokenStream

object StringPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ts.next().map { (tok, next) ->
            val t = tok as StringLiteralToken
            LiteralString(t.literal, t.span) to next
        }
}
