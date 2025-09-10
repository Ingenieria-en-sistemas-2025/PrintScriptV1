package org.printscript.parser.expr

import org.printscript.ast.Variable
import org.printscript.token.IdentifierToken
import org.printscript.token.TokenStream

object IdentifierPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ts.next().map { (token, next) ->
            val tok = token as IdentifierToken
            Variable(tok.identifier, tok.span) to next
        }
}
