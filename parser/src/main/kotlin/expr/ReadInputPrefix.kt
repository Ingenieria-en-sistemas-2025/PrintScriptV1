package expr

import ReadInput
import Span
import TokenStream

object ReadInputPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ParserUtils.expectKeyword(ts, Keyword.READ_INPUT).flatMap { (kw, t1) ->
            ParserUtils.expectSeparator(t1, Separator.LPAREN).flatMap { (_, t2) ->
                p.parseExpression(t2).flatMap { (arg, t3) ->
                    ParserUtils.expectSeparator(t3, Separator.RPAREN).map { (rp, t4) ->
                        ReadInput(arg, Span(kw.span.start, rp.span.end)) to t4
                    }
                }
            }
        }
}
