package expr

import ReadEnv
import Span
import TokenStream

object ReadEnvPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ParserUtils.expectKeyword(ts, Keyword.READ_ENV).flatMap { (kw, t1) ->
            ParserUtils.expectSeparator(t1, Separator.LPAREN).flatMap { (_, t2) ->
                p.parse(t2, Prec.LOWEST).flatMap { (arg, t3) ->
                    ParserUtils.expectSeparator(t3, Separator.RPAREN).map { (rp, t4) ->
                        ReadEnv(arg, Span(kw.span.start, rp.span.end)) to t4
                    }
                }
            }
        }
}
