package expr

import Binary
import Expression
import Operator
import Span
import TokenStream

class BinaryInfix(private val op: Operator, override val prec: Prec) : InfixParselet {
    override fun parse(p: ExpressionParser, left: Expression, ts: TokenStream) =
        ParserUtils.expectOperator(ts, op).flatMap { (_, t1) ->
            p.parseWith(t1, prec).map { (right, t2) ->
                Binary(left, right, op, Span(left.span.start, right.span.end)) to t2
            }
        }
}
