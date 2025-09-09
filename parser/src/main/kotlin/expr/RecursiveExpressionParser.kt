package expr

import Binary
import Expression
import Failure
import Grouping
import IdentifierToken
import KeywordToken
import LabeledError
import LiteralNumber
import LiteralString
import NumberLiteralToken
import Operator
import OperatorToken
import ParserUtils.expectKeyword
import ParserUtils.expectOperator
import ParserUtils.expectSeparator
import ReadEnv
import ReadInput
import Result
import Separator
import SeparatorToken
import Span
import StringLiteralToken
import Success
import TokenStream
import Variable

class RecursiveExpressionParser : ExpressionParser {

    override fun parseExpression(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        // una expresion empieza con un termino, despues puede venir una tail de +/-
        // parseTerm(ts) si es success, el pair (nodoAST, restoTs) desempaqueta en left0 y t0
        // usa parseExprTail(left0, t0) para ver si hay un + o - adelante, si hay, consume operador,
        // parsea otro termino y construye nodo Binary y llama recursivamente para acumular mas + o -
        parseTerm(ts).flatMap { (left0, t0) -> parseExprTail(left0, t0) }

    // acumula + y - en la izquierda
    private fun parseExprTail(left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        ts.peek().flatMap { token -> // mira el proximo token sin consumir
            when { // si falla, corta, si no da el token
                token is OperatorToken && (token.operator == Operator.PLUS || token.operator == Operator.MINUS) -> {
                    // si es un + o -, hay mas cola de la expresion
                    val operator = token.operator
                    expectOperator(ts, operator).flatMap { (_, t1) -> // consume el operator. t1 es el stream avanzado
                        parseTerm(t1).flatMap { (right, t2) -> // parsea el term que viene a la derecha del operador
                            val node = Binary(left, right, operator, Span(left.span.start, right.span.end))
                            parseExprTail(node, t2) // si hay mas + o -, se vuelve a acumular, si no termina
                        }
                    }
                }
                else -> Success(left to ts)
            }
        }

    private fun parseTerm(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        parseFactor(ts).flatMap { (left0, t0) -> parseTermTail(left0, t0) }

    // acumula * y / en la izquierda
    private fun parseTermTail(left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        ts.peek().flatMap { token -> // se peek falla, corta, sino devuelve token
            when {
                token is OperatorToken &&
                    (token.operator == Operator.MULTIPLY || token.operator == Operator.DIVIDE) -> {
                    // si es un * o /, hay mas cola del termino
                    val operator = token.operator
                    expectOperator(ts, operator).flatMap { (_, t1) -> // consume el operador
                        parseFactor(t1).flatMap { (right, t2) -> // parsea el factor a la derecha del op
                            val node = Binary(left, right, operator, Span(left.span.start, right.span.end))
                            parseTermTail(node, t2) // sigue con la tail a ver si hay mas * o /
                        }
                    }
                }
                else -> Success(left to ts) // si no hay mas, termina
            }
        }

    // factor: numero | string | id | ( expr )
    private fun parseFactor(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            when (token) {
                is NumberLiteralToken ->
                    ts.next().map { pair ->
                        val token = pair.first as NumberLiteralToken
                        val nextStream = pair.second
                        Pair(LiteralNumber(token.raw, token.span), nextStream)
                    }

                is StringLiteralToken ->
                    ts.next().map { pair ->
                        val token = pair.first as StringLiteralToken
                        val nextStream = pair.second
                        Pair(LiteralString(token.literal, token.span), nextStream)
                    }

                is IdentifierToken ->
                    ts.next().map { pair ->
                        val token = pair.first as IdentifierToken
                        val nextStream = pair.second
                        Pair(Variable(token.identifier, token.span), nextStream)
                    }

                is KeywordToken -> when (token.kind) {
                    Keyword.READ_INPUT -> parseReadInput(ts)
                    Keyword.READ_ENV -> parseReadEnv(ts)
                    else -> Failure(LabeledError.of(token.span, "Expresión inesperada: $token"))
                }

                is SeparatorToken ->
                    if (token.separator == Separator.LPAREN) {
                        parseParenExpr(ts)
                    } else {
                        Failure(LabeledError.of(token.span, "Expresión inesperada: $token"))
                    }

                else -> Failure(LabeledError.of(token.span, "Expresión inesperada: $token"))
            }
        }

    private fun parseParenExpr(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        expectSeparator(ts, Separator.LPAREN).flatMap { (lpar, t1) ->
            parseExpression(t1).flatMap { (inner, t2) ->
                expectSeparator(t2, Separator.RPAREN).map { (rpar, t3) ->
                    Grouping(inner, Span(lpar.span.start, rpar.span.end)) to t3
                }
            }
        }
    private fun parseReadInput(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        expectKeyword(ts, Keyword.READ_INPUT).flatMap { (kw, t1) ->
            expectSeparator(t1, Separator.LPAREN).flatMap { (_, t2) ->
                parseExpression(t2).flatMap { (arg, t3) ->
                    expectSeparator(t3, Separator.RPAREN).map { (rp, t4) ->
                        ReadInput(arg, Span(kw.span.start, rp.span.end)) to t4
                    }
                }
            }
        }

    private fun parseReadEnv(ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError> =
        expectKeyword(ts, Keyword.READ_ENV).flatMap { (kw, t1) ->
            expectSeparator(t1, Separator.LPAREN).flatMap { (_, t2) ->
                parseExpression(t2).flatMap { (arg, t3) ->
                    expectSeparator(t3, Separator.RPAREN).map { (rp, t4) ->
                        ReadEnv(arg, Span(kw.span.start, rp.span.end)) to t4
                    }
                }
            }
        }
}
