package expr

import Binary
import Expression
import Grouping
import IdentifierToken
import LiteralNumber
import LiteralString
import NumberLiteralToken
import OperatorToken
import SeparatorToken
import Span
import StringLiteralToken
import TokenStream
import Variable

class RecursiveExpressionParser : ExpressionParser {
    override fun parseExpression(tokenStream: TokenStream): Expression {
        var left = parseTerm(tokenStream) // Primero un term
        while (true) { // Segundo, + y - en un bucle
            val token = tokenStream.peek()
            val isPlusMinus = token is OperatorToken &&
                (token.operator == Operator.PLUS || token.operator == Operator.MINUS)
            if (!isPlusMinus) break // Si no hay + o -, termina
            val operator = (tokenStream.next() as OperatorToken).operator // Consume + o -
            val right = parseTerm(tokenStream) // Se lee OTRO term a la derecha
            val span = Span(left.span.start, right.span.end)
            left = Binary(left, right, operator, span) // Encadenamiento left, right y operador
        }
        return left
    }

    private fun parseTerm(tokenStream: TokenStream): Expression {
        var left = parseFactor(tokenStream) // Primero un factor
        while (true) { // Segundo, * o / en un bucle
            val token = tokenStream.peek()
            val isMulDiv = token is OperatorToken &&
                (token.operator == Operator.MULTIPLY || token.operator == Operator.DIVIDE)
            if (!isMulDiv) break
            val operator = (tokenStream.next() as OperatorToken).operator // Consume * o /
            val right = parseFactor(tokenStream) // Lee OTRO factor a derecha
            val span = Span(left.span.start, right.span.end)
            left = Binary(left, right, operator, span) // Encadena
        }
        return left
    }

    private fun parseFactor(tokenStream: TokenStream): Expression =
        when (val token = tokenStream.peek()) {
            is NumberLiteralToken -> {
                tokenStream.next()
                LiteralNumber(token.raw, token.span)
            }
            is StringLiteralToken -> {
                tokenStream.next()
                LiteralString(token.literal, token.span)
            }
            is IdentifierToken -> {
                tokenStream.next()
                Variable(token.identifier, token.span)
            }
            is SeparatorToken -> {
                // Si hay parentesis, se debe parsear la expresion adentro (recursivo)
                if (token.separator == Separator.LPAREN) {
                    val lpar = tokenStream.next() as SeparatorToken
                    val expr = parseExpression(tokenStream)
                    val rpar = tokenStream.expectSep(Separator.RPAREN)
                    Grouping(expr, Span(lpar.span.start, rpar.span.end))
                } else {
                    error("Expresión inesperada: $token")
                }
            }
            else -> error("Expresión inesperada: $token")
        }
}
