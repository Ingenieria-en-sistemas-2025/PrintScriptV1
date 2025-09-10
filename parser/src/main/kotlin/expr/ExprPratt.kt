package expr
import Expression
import Failure
import Keyword
import KeywordToken
import LabeledError
import Operator
import OperatorToken
import Result
import Separator
import SeparatorToken
import Success
import Token
import TokenStream

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
        val result0 = peekNext(ts0)
        return when (result0) {
            is Failure -> result0
            is Success -> {
                val token0 = result0.value
                val prefix = pickPrefixFor(token0)
                    ?: return startError(token0)

                val rPrefix = parsePrefix(prefix, ts0)
                when (rPrefix) {
                    is Failure -> rPrefix
                    is Success -> {
                        val (left, afterPrefix) = rPrefix.value
                        chainInfix(left, afterPrefix, minPrec)
                    }
                }
            }
        }
    }

    /** Continue: while the next infix beats minPrec, apply it. */
    private fun chainInfix(
        left: Expression,
        ts: TokenStream,
        minPrec: Prec,
    ): Result<Pair<Expression, TokenStream>, LabeledError> {
        val rPeek = peekNext(ts)
        return when (rPeek) {
            is Failure -> rPeek
            is Success -> {
                val nextToken = rPeek.value
                val infix = pickInfixFor(nextToken) ?: return done(left, ts)
                if (shouldStopDueToPrecedence(infix, minPrec)) return done(left, ts)

                val rStep = applyInfixOnce(infix, left, ts)
                when (rStep) {
                    is Failure -> rStep
                    is Success -> {
                        val (newLeft, afterInfix) = rStep.value
                        chainInfix(newLeft, afterInfix, minPrec)
                    }
                }
            }
        }
    }

    /** Look at the next token without consuming it. */
    private fun peekNext(ts: TokenStream) = ts.peek()

    /** Pick how the expression starts: by keyword, separator or token kind. */
    private fun pickPrefixFor(t0: Token): PrefixParselet? = when (t0) {
        is KeywordToken -> prefixByKeyword[t0.kind]
        is SeparatorToken -> prefixBySeparator[t0.separator] // e.g., '(' → GroupingPrefix
        else -> classifyKind(t0)?.let(prefixByTokenKind::get)
    }

    /** Pick the infix operator that continues (operators only). */
    private fun pickInfixFor(t: Token): InfixParselet? = when (t) {
        is OperatorToken -> infixByOperator[t.operator] // e.g., +, -, *, /
        else -> null
    }

    /** Stop if the infix is too weak for the current context. */
    private fun shouldStopDueToPrecedence(infix: InfixParselet, minPrec: Prec): Boolean =
        infix.prec.priority <= minPrec.priority

    /** Apply a single infix: combine left with its right-hand side. */
    private fun applyInfixOnce(
        infix: InfixParselet,
        left: Expression,
        ts: TokenStream,
    ) = infix.parse(this, left, ts)

    /** Parse the chosen prefix at the start point. */
    private fun parsePrefix(prefix: PrefixParselet, ts: TokenStream) =
        prefix.parse(this, ts)

    /** Final result: no more infix wins. */
    private fun done(left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        Success(left to ts)

    /** Clear error when we don't know how to start with that token. */
    private fun startError(t0: Token): Result<Pair<Expression, TokenStream>, LabeledError> =
        Failure(LabeledError.of(t0.span, "Inicio de expresión no reconocido: $t0"))
}
