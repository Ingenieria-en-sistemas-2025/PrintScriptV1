package org.printscript.parser.stmt

import org.printscript.ast.Assignment
import org.printscript.ast.Expression
import org.printscript.ast.Statement
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils.expectIdentifier
import org.printscript.parser.ParserUtils.expectOperator
import org.printscript.parser.ParserUtils.expectSeparator
import org.printscript.parser.expr.ExpressionParser
import org.printscript.token.IdentifierToken
import org.printscript.token.SeparatorToken
import org.printscript.token.TokenStream

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
