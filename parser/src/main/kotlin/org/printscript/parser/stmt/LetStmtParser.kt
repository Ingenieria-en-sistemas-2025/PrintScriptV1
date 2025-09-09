package org.printscript.parser.stmt

import org.printscript.ast.Expression
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration
import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.parser.ParserUtils.consumeIfOperator
import org.printscript.parser.ParserUtils.expectIdentifier
import org.printscript.parser.ParserUtils.expectKeyword
import org.printscript.parser.ParserUtils.expectSeparator
import org.printscript.parser.ParserUtils.expectTypeToken
import org.printscript.parser.expr.ExpressionParser
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.SeparatorToken
import org.printscript.token.TokenStream
import org.printscript.token.TypeToken

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
