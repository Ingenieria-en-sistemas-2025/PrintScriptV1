package org.printscript.parser.factories

import org.printscript.ast.Expression
import org.printscript.common.Failure
import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.parser.FirstParser
import org.printscript.parser.Parser
import org.printscript.parser.expr.BinaryInfix
import org.printscript.parser.expr.ExprPratt
import org.printscript.parser.expr.ExpressionParser
import org.printscript.parser.expr.GroupingPrefix
import org.printscript.parser.expr.IdentifierPrefix
import org.printscript.parser.expr.InfixParselet
import org.printscript.parser.expr.NumberPrefix
import org.printscript.parser.expr.Prec
import org.printscript.parser.expr.PrefixParselet
import org.printscript.parser.expr.StringPrefix
import org.printscript.parser.head.Assign
import org.printscript.parser.head.FirstHeadDetector
import org.printscript.parser.head.Kw
import org.printscript.parser.stmt.AssignmentStmtParser
import org.printscript.parser.stmt.ConstDeclStmtParser
import org.printscript.parser.stmt.LetStmtParser
import org.printscript.parser.stmt.PrintlnStmtParser
import org.printscript.token.IdentifierToken
import org.printscript.token.NumberLiteralToken
import org.printscript.token.OperatorToken
import org.printscript.token.SeparatorToken
import org.printscript.token.StringLiteralToken
import org.printscript.token.Token
import org.printscript.token.TokenStream

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
