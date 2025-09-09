package factories

import Expression
import Failure
import FirstParser
import OperatorToken
import Parser
import Token
import TokenStream
import expr.BinaryInfix
import expr.ExprPratt
import expr.ExpressionParser
import expr.GroupingPrefix
import expr.IdentifierPrefix
import expr.InfixParselet
import expr.NumberPrefix
import expr.Prec
import expr.PrefixParselet
import expr.StringPrefix
import head.Assign
import head.FirstHeadDetector
import head.Kw
import stmt.AssignmentStmtParser
import stmt.ConstDeclStmtParser
import stmt.LetStmtParser
import stmt.PrintlnStmtParser

object ParserFactoryV10 {
    fun create(): Parser {
        val head = FirstHeadDetector()
        val expr = createExpressionParser()

        val stmtParsers = buildMap {
            put(Kw(Keyword.LET), LetStmtParser)
            put(Kw(Keyword.PRINTLN), PrintlnStmtParser)
            put(Assign, AssignmentStmtParser)
            put(Kw(Keyword.CONST), ConstDeclStmtParser)
        }

        return FirstParser(head, expr, stmtParsers)
    }

    private fun createExpressionParser(): ExpressionParser {
        val prefix = buildMap {
            put(NumberLiteralToken::class.java, NumberPrefix)
            put(StringLiteralToken::class.java, StringPrefix)
            put(IdentifierToken::class.java, IdentifierPrefix)
            put(
                SeparatorToken::class.java,
                object : PrefixParselet {
                    override fun parse(p: ExprPratt, ts: TokenStream) = GroupingPrefix.parse(p, ts)
                },
            )
        }

        val infix = buildMap<Class<out Token>, InfixParselet> {
            put(
                OperatorToken::class.java,
                object : InfixParselet {
                    override val prec: Prec get() = Prec.MUL

                    override fun parse(p: ExprPratt, left: Expression, ts: TokenStream) =
                        ts.peek().flatMap { token ->
                            when ((token as OperatorToken).operator) {
                                Operator.PLUS -> BinaryInfix(Operator.PLUS, Prec.ADD).parse(p, left, ts)
                                Operator.MINUS -> BinaryInfix(Operator.MINUS, Prec.ADD).parse(p, left, ts)
                                Operator.MULTIPLY -> BinaryInfix(Operator.MULTIPLY, Prec.MUL).parse(p, left, ts)
                                Operator.DIVIDE -> BinaryInfix(Operator.DIVIDE, Prec.MUL).parse(p, left, ts)
                                else -> Failure(LabeledError.of(token.span, "Operador no soportado: ${token.operator}"))
                            }
                        }
                },
            )
        }

        return ExprPratt(prefix, infix)
    }
}
