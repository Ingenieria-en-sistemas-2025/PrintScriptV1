package stmt

import Assignment
import Expression
import IdentifierToken
import LabeledError
import ParserUtils.expectIdentifier
import ParserUtils.expectOperator
import ParserUtils.expectSeparator
import Result
import SeparatorToken
import Span
import Statement
import TokenStream
import expr.ExpressionParser

object AssignmentStmtParser : StmtParser {
    override fun parse(
        ts: TokenStream,
        expressionParser: ExpressionParser,
    ): Result<Pair<Statement, TokenStream>, LabeledError> =
        parseIdentifier(ts).flatMap { (identifierToken, t1) ->
            parseEqual(t1).flatMap { t2 ->
                parseRight(expressionParser, t2).flatMap { (value, t3) ->
                    parseSemicolon(t3).map { (semicolon, t4) ->
                        val span = Span(identifierToken.span.start, semicolon.span.end)
                        Pair(Assignment(identifierToken.identifier, value, span), t4)
                    }
                }
            }
        }

    private fun parseIdentifier(ts: TokenStream): Result<Pair<IdentifierToken, TokenStream>, LabeledError> =
        expectIdentifier(ts)

    private fun parseEqual(ts: TokenStream): Result<TokenStream, LabeledError> =
        expectOperator(ts, Operator.ASSIGN).map { (_, nextTs) -> nextTs }

    private fun parseRight(
        expressionParser: ExpressionParser,
        ts: TokenStream,
    ): Result<Pair<Expression, TokenStream>, LabeledError> =
        expressionParser.parseExpression(ts)

    private fun parseSemicolon(ts: TokenStream): Result<Pair<SeparatorToken, TokenStream>, LabeledError> =
        expectSeparator(ts, Separator.SEMICOLON)
}
