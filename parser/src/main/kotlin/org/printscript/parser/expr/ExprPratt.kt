package org.printscript.parser.expr
import org.printscript.ast.Expression
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.token.Token
import org.printscript.token.TokenStream

class ExprPratt(
    private val prefix: Map<Class<out Token>, PrefixParselet>,
    private val infix: Map<Class<out Token>, InfixParselet>,
) : ExpressionParser {

    override fun parseExpression(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        parse(ts, Prec.LOWEST)

    fun parse(ts0: TokenStream, minPrec: Prec): Result<Pair<Expression, TokenStream>, LabeledError> =
        ts0.peek().flatMap { t0 ->
            val pre = prefix[t0.javaClass]
                ?: return@flatMap Failure(LabeledError.of(t0.span, "Inicio de expresión no reconocido: $t0"))
            pre.parse(this, ts0).flatMap { (left, ts1) -> parseInfix(left, ts1, minPrec) }
        }

    private fun parseInfix(left0: Expression, ts0: TokenStream, minPrec: Prec): Result<Pair<Expression, TokenStream>, LabeledError> =
        ts0.peek().flatMap { t ->
            val inf = infix[t.javaClass] ?: return@flatMap Success(left0 to ts0)
            if (inf.prec.priority <= minPrec.priority) return@flatMap Success(left0 to ts0)
            inf.parse(this, left0, ts0).flatMap { (newLeft, ts1) -> parseInfix(newLeft, ts1, minPrec) }
        }
}
