package stmt

import Expression
import IdentifierToken
import Keyword
import KeywordToken
import LabeledError
import Operator
import ParserUtils.consumeIfOperator
import ParserUtils.expectIdentifier
import ParserUtils.expectKeyword
import ParserUtils.expectSeparator
import ParserUtils.expectTypeToken
import Result
import Separator
import SeparatorToken
import Span
import Statement
import Success
import TokenStream
import TypeToken
import VarDeclaration
import expr.ExpressionParser

// let nombre: tipo (= expr)? ;
object LetStmtParser : StmtParser {

    override fun parse(
        ts: TokenStream,
        expressionParser: ExpressionParser,
    ): Result<Pair<Statement, TokenStream>, LabeledError> =
        parseLetKw(ts).flatMap { (_, t1) ->
            parseName(t1).flatMap { (nameToken, t2) ->
                parseColon(t2).flatMap { t3 ->
                    parseType(t3).flatMap { (typeTok, t4) ->
                        parseOptionalInit(expressionParser, t4).flatMap { (initExpr, t5) ->
                            parseSemicolon(t5).map { (semicolon, t6) ->
                                val span = Span(nameToken.span.start, semicolon.span.end)
                                Pair((VarDeclaration(nameToken.identifier, typeTok.type, initExpr, span)), t6)
                            }
                        }
                    }
                }
            }
        }

    private fun parseLetKw(ts: TokenStream): Result<Pair<KeywordToken, TokenStream>, LabeledError> =
        expectKeyword(ts, Keyword.LET)

    private fun parseName(ts: TokenStream): Result<Pair<IdentifierToken, TokenStream>, LabeledError> =
        expectIdentifier(ts)

    private fun parseColon(ts: TokenStream): Result<TokenStream, LabeledError> =
        expectSeparator(ts, Separator.COLON).map { (_, nextTs) -> nextTs }

    private fun parseType(ts: TokenStream): Result<Pair<TypeToken, TokenStream>, LabeledError> =
        expectTypeToken(ts)

    private fun parseOptionalInit(
        expressionParser: ExpressionParser,
        ts: TokenStream,
    ): Result<Pair<Expression?, TokenStream>, LabeledError> =
        consumeIfOperator(ts, Operator.ASSIGN).flatMap { (hasEq, t1) ->
            if (!hasEq) {
                Success(null to t1)
            } else {
                expressionParser.parseExpression(t1).map { (expression, t2) -> expression to t2 }
            }
        }

    private fun parseSemicolon(ts: TokenStream): Result<Pair<SeparatorToken, TokenStream>, LabeledError> =
        expectSeparator(ts, Separator.SEMICOLON)
}
