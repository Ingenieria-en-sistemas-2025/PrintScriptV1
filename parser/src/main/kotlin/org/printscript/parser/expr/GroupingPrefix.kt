package org.printscript.parser.expr

import org.printscript.ast.Grouping
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils
import org.printscript.token.TokenStream

object GroupingPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ParserUtils.expectSeparator(ts, Separator.LPAREN).flatMap { (lp, t1) ->
            p.parseExpression(t1).flatMap { (inner, t2) ->
                ParserUtils.expectSeparator(t2, Separator.RPAREN).map { (rp, t3) ->
                    Grouping(inner, Span(lp.span.start, rp.span.end)) to t3
                }
            }
        }
}
