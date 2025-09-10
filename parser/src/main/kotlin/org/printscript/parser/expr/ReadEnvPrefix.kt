package org.printscript.parser.expr

import org.printscript.ast.ReadEnv
import org.printscript.common.Keyword
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils
import org.printscript.token.TokenStream

object ReadEnvPrefix : PrefixParselet {
    override fun parse(p: ExpressionParser, ts: TokenStream) =
        ParserUtils.expectKeyword(ts, Keyword.READ_ENV).flatMap { (kw, t1) ->
            ParserUtils.expectSeparator(t1, Separator.LPAREN).flatMap { (_, t2) ->
                p.parseExpression(t2).flatMap { (arg, t3) ->
                    ParserUtils.expectSeparator(t3, Separator.RPAREN).map { (rp, t4) ->
                        ReadEnv(arg, Span(kw.span.start, rp.span.end)) to t4
                    }
                }
            }
        }
}
