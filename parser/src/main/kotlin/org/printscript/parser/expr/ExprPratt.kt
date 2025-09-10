package org.printscript.parser.expr
import org.printscript.ast.Expression
import org.printscript.common.Failure
import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Success
import org.printscript.token.KeywordToken
import org.printscript.token.OperatorToken
import org.printscript.token.SeparatorToken
import org.printscript.token.Token
import org.printscript.token.TokenStream

class ExprPratt(
    private val prefixByKeyword: Map<Keyword, PrefixParselet>,
    private val prefixByTokenKind: Map<TokenKind, PrefixParselet>,
    private val prefixBySeparator: Map<Separator, PrefixParselet>,
    private val infixByOperator: Map<Operator, InfixParselet>,
) : ExpressionParser {

    override fun parseExpression(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        parseWith(ts, Prec.LOWEST)

    // Con precedencia
    override fun parseWith(ts: TokenStream, minPrec: Prec): Result<Pair<Expression, TokenStream>, LabeledError> =
        startExpression(ts, minPrec)

    // Primero: se elije el prefijo y se parsea, despues se sigue con los infix
    private fun startExpression(ts0: TokenStream, minPrec: Prec): Result<Pair<Expression, TokenStream>, LabeledError> {
        return when (val result0 = peekNext(ts0)) {
            is Failure -> result0
            is Success -> {
                val token0 = result0.value
                val prefix = pickPrefixFor(token0)
                    ?: return startError(token0)

                when (val rPrefix = parsePrefix(prefix, ts0)) {
                    is Failure -> rPrefix
                    is Success -> {
                        val (left, afterPrefix) = rPrefix.value
                        chainInfix(left, afterPrefix, minPrec)
                    }
                }
            }
        }
    }

    private fun chainInfix(
        left: Expression,
        ts: TokenStream,
        minPrec: Prec,
    ): Result<Pair<Expression, TokenStream>, LabeledError> {
        return when (val resultPeek = peekNext(ts)) {
            is Failure -> resultPeek
            is Success -> {
                val nextToken = resultPeek.value
                val infix = pickInfixFor(nextToken) ?: return done(left, ts)
                if (shouldStopDueToPrecedence(infix, minPrec)) return done(left, ts)

                when (val resultStep = applyInfixOnce(infix, left, ts)) {
                    is Failure -> resultStep
                    is Success -> {
                        val (newLeft, afterInfix) = resultStep.value
                        chainInfix(newLeft, afterInfix, minPrec)
                    }
                }
            }
        }
    }

    private fun peekNext(ts: TokenStream) = ts.peek()

    private fun pickPrefixFor(t0: Token): PrefixParselet? = when (t0) {
        is KeywordToken -> prefixByKeyword[t0.kind]
        is SeparatorToken -> prefixBySeparator[t0.separator]
        else -> classifyKind(t0)?.let(prefixByTokenKind::get)
    }

    private fun pickInfixFor(t: Token): InfixParselet? = when (t) {
        is OperatorToken -> infixByOperator[t.operator]
        else -> null
    }

    private fun shouldStopDueToPrecedence(infix: InfixParselet, minPrec: Prec): Boolean =
        infix.prec.priority <= minPrec.priority

    private fun applyInfixOnce(
        infix: InfixParselet,
        left: Expression,
        ts: TokenStream,
    ) = infix.parse(this, left, ts)

    private fun parsePrefix(prefix: PrefixParselet, ts: TokenStream) =
        prefix.parse(this, ts)

    private fun done(left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        Success(left to ts)

    private fun startError(t0: Token): Result<Pair<Expression, TokenStream>, LabeledError> =
        Failure(LabeledError.of(t0.span, "Inicio de expresión no reconocido: $t0"))
}
