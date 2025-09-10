package factories

import FirstParser
import Keyword
import Operator
import Parser
import Separator
import expr.BinaryInfix
import expr.BooleanPrefix
import expr.ExprPratt
import expr.ExpressionParser
import expr.GroupingPrefix
import expr.IdentifierPrefix
import expr.InfixParselet
import expr.NumberPrefix
import expr.Prec
import expr.PrefixParselet
import expr.ReadEnvPrefix
import expr.ReadInputPrefix
import expr.StringPrefix
import expr.TokenKind
import head.Assign
import head.FirstHeadDetector
import head.Head
import head.Kw
import stmt.AssignmentStmtParser
import stmt.ConstDeclStmtParser
import stmt.IfStmtParser
import stmt.LetStmtParser
import stmt.PrintlnStmtParser
import stmt.StmtParser

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
