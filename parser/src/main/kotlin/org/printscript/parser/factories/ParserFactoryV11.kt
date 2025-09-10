package org.printscript.parser.factories

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
import org.printscript.parser.FirstParser
import org.printscript.parser.Parser
import org.printscript.parser.expr.BinaryInfix
import org.printscript.parser.expr.BooleanPrefix
import org.printscript.parser.expr.ExprPratt
import org.printscript.parser.expr.ExpressionParser
import org.printscript.parser.expr.GroupingPrefix
import org.printscript.parser.expr.IdentifierPrefix
import org.printscript.parser.expr.InfixParselet
import org.printscript.parser.expr.NumberPrefix
import org.printscript.parser.expr.Prec
import org.printscript.parser.expr.PrefixParselet
import org.printscript.parser.expr.ReadEnvPrefix
import org.printscript.parser.expr.ReadInputPrefix
import org.printscript.parser.expr.StringPrefix
import org.printscript.parser.expr.TokenKind
import org.printscript.parser.head.Assign
import org.printscript.parser.head.FirstHeadDetector
import org.printscript.parser.head.Head
import org.printscript.parser.head.Kw
import org.printscript.parser.stmt.AssignmentStmtParser
import org.printscript.parser.stmt.ConstDeclStmtParser
import org.printscript.parser.stmt.IfStmtParser
import org.printscript.parser.stmt.LetStmtParser
import org.printscript.parser.stmt.PrintlnStmtParser
import org.printscript.parser.stmt.StmtParser

object ParserFactoryV11 {
    fun create(): Parser {
        val head = FirstHeadDetector()
        val expr = createExpressionParser()

        val stmtParsers = buildMap<Head, StmtParser> {
            put(Kw(Keyword.LET), LetStmtParser)
            put(Kw(Keyword.PRINTLN), PrintlnStmtParser)
            put(Assign, AssignmentStmtParser)
            put(Kw(Keyword.CONST), ConstDeclStmtParser)
            put(Kw(Keyword.IF), IfStmtParser(head, this))
        }

        return FirstParser(head, expr, stmtParsers)
    }

    private fun createExpressionParser(): ExpressionParser {
        val prefixByKeyword: Map<Keyword, PrefixParselet> = mapOf(
            Keyword.READ_ENV to ReadEnvPrefix,
            Keyword.READ_INPUT to ReadInputPrefix,
        )

        val prefixByTokenKind: Map<TokenKind, PrefixParselet> = mapOf(
            TokenKind.NUMBER to NumberPrefix,
            TokenKind.STRING to StringPrefix,
            TokenKind.IDENT to IdentifierPrefix,
            TokenKind.BOOLEAN to BooleanPrefix,
        )

        val prefixBySeparator: Map<Separator, PrefixParselet> = mapOf(
            Separator.LPAREN to GroupingPrefix,
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
