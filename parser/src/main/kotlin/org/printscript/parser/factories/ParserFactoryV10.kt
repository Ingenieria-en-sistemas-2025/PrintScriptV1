package org.printscript.parser.factories

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
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
import org.printscript.parser.expr.TokenKind
import org.printscript.parser.head.Assign
import org.printscript.parser.head.FirstHeadDetector
import org.printscript.parser.head.Kw
import org.printscript.parser.stmt.AssignmentStmtParser
import org.printscript.parser.stmt.ConstDeclStmtParser
import org.printscript.parser.stmt.LetStmtParser
import org.printscript.parser.stmt.PrintlnStmtParser

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
        // En V10 no hay keywords prefix (como readEnv/readInput)
        val prefixByKeyword: Map<Keyword, PrefixParselet> = emptyMap()

        val prefixByTokenKind: Map<TokenKind, PrefixParselet> = mapOf(
            TokenKind.NUMBER to NumberPrefix,
            TokenKind.STRING to StringPrefix,
            TokenKind.IDENT to IdentifierPrefix,
        )

        val prefixBySeparator: Map<Separator, PrefixParselet> = mapOf(
            Separator.LPAREN to GroupingPrefix, // "(" expr ")"
        )

        val infixByOperator: Map<Operator, InfixParselet> = mapOf(
            Operator.PLUS to BinaryInfix(Operator.PLUS, Prec.ADD),
            Operator.MINUS to BinaryInfix(Operator.MINUS, Prec.ADD),
            Operator.MULTIPLY to BinaryInfix(Operator.MULTIPLY, Prec.MUL),
            Operator.DIVIDE to BinaryInfix(Operator.DIVIDE, Prec.MUL),
        )

        return ExprPratt(
            prefixByKeyword = prefixByKeyword,
            prefixByTokenKind = prefixByTokenKind,
            prefixBySeparator = prefixBySeparator,
            infixByOperator = infixByOperator,
        )
    }
}
