package org.printscript.parser.expr

import org.printscript.ast.Binary
import org.printscript.ast.Expression
import org.printscript.common.Operator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils
import org.printscript.token.TokenStream

class BinaryInfix(private val op: Operator, override val prec: Prec) : InfixParselet {
    override fun parse(p: ExprPratt, left: Expression, ts: TokenStream) =
        ParserUtils.expectOperator(ts, op).flatMap { (tok, t1) ->
            p.parse(t1, prec).map { (right, t2) ->
                Binary(left, right, op, Span(left.span.start, right.span.end)) to t2
            }
        }
}
