package org.printscript.parser.expr

import org.printscript.ast.LiteralNumber
import org.printscript.token.NumberLiteralToken
import org.printscript.token.TokenStream

object NumberPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as NumberLiteralToken
            LiteralNumber(tok.raw, tok.span) to next
        }
}
