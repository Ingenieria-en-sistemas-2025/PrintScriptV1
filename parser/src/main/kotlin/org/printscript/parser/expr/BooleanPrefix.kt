package org.printscript.parser.expr

import org.printscript.ast.LiteralBoolean
import org.printscript.token.BooleanLiteralToken
import org.printscript.token.TokenStream

object BooleanPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as BooleanLiteralToken
            LiteralBoolean(tok.value, tok.span) to next
        }
}
