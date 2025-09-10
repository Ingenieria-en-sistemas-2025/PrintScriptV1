package expr

import Grouping
import Span
import TokenStream

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
