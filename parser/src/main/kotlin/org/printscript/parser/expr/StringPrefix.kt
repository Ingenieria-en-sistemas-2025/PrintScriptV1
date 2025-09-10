package org.printscript.parser.expr

import org.printscript.ast.LiteralString
import org.printscript.token.StringLiteralToken
import org.printscript.token.TokenStream

object StringPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as StringLiteralToken
            LiteralString(tok.literal, tok.span) to next
        }
}
