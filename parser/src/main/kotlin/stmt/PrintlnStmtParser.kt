package stmt

import Keyword
import KeywordToken
import LabeledError
import ParserUtils.expectKeyword
import ParserUtils.expectSeparator
import Println
import Result
import Separator
import SeparatorToken
import Span
import Statement
import TokenStream
import expr.ExpressionParser

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
