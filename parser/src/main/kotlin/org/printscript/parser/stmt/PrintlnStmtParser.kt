package org.printscript.parser.stmt

import org.printscript.ast.Println
import org.printscript.ast.Statement
import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils.expectKeyword
import org.printscript.parser.ParserUtils.expectSeparator
import org.printscript.parser.expr.ExpressionParser
import org.printscript.token.KeywordToken
import org.printscript.token.SeparatorToken
import org.printscript.token.TokenStream

// println(expr) ;
object PrintlnStmtParser : StmtParser {

    override fun parse(
        ts: TokenStream,
        expressionParser: ExpressionParser,
    ): Result<Pair<Statement, TokenStream>, LabeledError> =
        parseKw(ts).flatMap { (kwToken, t1) ->
            parseLparen(t1).flatMap { t2 ->
                expressionParser.parseExpression(t2).flatMap { (arg, t3) ->
                    parseRparen(t3).flatMap { t4 ->
                        parseSemicolon(t4).map { (semicolon, t5) ->
                            val span = Span(kwToken.span.start, semicolon.span.end)
                            Pair(Println(arg, span), t5)
                        }
                    }
                }
            }
        }

    // println
    private fun parseKw(ts: TokenStream): Result<Pair<KeywordToken, TokenStream>, LabeledError> =
        expectKeyword(ts, Keyword.PRINTLN)

    // (
    private fun parseLparen(ts: TokenStream): Result<TokenStream, LabeledError> =
        expectSeparator(ts, Separator.LPAREN).map { (_, nextTs) -> nextTs }

    // )
    private fun parseRparen(ts: TokenStream): Result<TokenStream, LabeledError> =
        expectSeparator(ts, Separator.RPAREN).map { (_, nextTs) -> nextTs }

    // ;
    private fun parseSemicolon(ts: TokenStream): Result<Pair<SeparatorToken, TokenStream>, LabeledError> =
        expectSeparator(ts, Separator.SEMICOLON)
}
