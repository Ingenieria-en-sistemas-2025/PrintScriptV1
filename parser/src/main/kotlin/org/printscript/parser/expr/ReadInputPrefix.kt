package org.printscript.parser.expr

import org.printscript.ast.ReadInput
import org.printscript.common.Keyword
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils
import org.printscript.token.TokenStream

object ReadInputPrefix : PrefixParselet {
    override fun parse(p: ExprPratt, ts: TokenStream) =
        ParserUtils.expectKeyword(ts, Keyword.READ_INPUT).flatMap { (kw, t1) ->
            ParserUtils.expectSeparator(t1, Separator.LPAREN).flatMap { (_, t2) ->
                p.parse(t2, Prec.LOWEST).flatMap { (arg, t3) ->
                    ParserUtils.expectSeparator(t3, Separator.RPAREN).map { (rp, t4) ->
                        ReadInput(arg, Span(kw.span.start, rp.span.end)) to t4
                    }
                }
            }
        }
}
