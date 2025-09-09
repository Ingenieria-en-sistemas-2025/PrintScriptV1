package org.printscript.parser.stmt

import org.printscript.ast.IfStmt
import org.printscript.ast.Statement
import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.parser.ParserUtils.expectKeyword
import org.printscript.parser.ParserUtils.expectSeparator
import org.printscript.parser.Statements
import org.printscript.parser.expr.ExpressionParser
import org.printscript.parser.head.Head
import org.printscript.parser.head.HeadDetector
import org.printscript.token.KeywordToken
import org.printscript.token.SeparatorToken
import org.printscript.token.TokenStream

class IfStmtParser(
    private val headDetector: HeadDetector,
    private val stmtParsers: Map<Head, StmtParser>,
) : StmtParser {

    override fun parse(ts: TokenStream, expressionParser: ExpressionParser): Result<Pair<Statement, TokenStream>, LabeledError> =
        expectKeyword(ts, Keyword.IF).flatMap { (ifKw, t1) ->
            lparen(t1).flatMap { t2 ->
                expressionParser.parseExpression(t2).flatMap { (cond, t3) ->
                    rparen(t3).flatMap { t4 ->
                        lbrace(t4).flatMap { t5 ->
                            // THEN { ... }
                            Statements.parseUntil(
                                t5,
                                headDetector,
                                stmtParsers,
                                expressionParser,
                                isTerminator = { it is SeparatorToken && it.separator == Separator.RBRACE },
                            ).flatMap { (thenStmts, t6) ->
                                rbrace(t6).flatMap { (rThen, t7) ->
                                    // else opcional
                                    maybeElse(t7).flatMap { (hasElse, t8) ->
                                        if (!hasElse) {
                                            Success(IfStmt(cond, thenStmts, null, Span(ifKw.span.start, rThen.span.end)) to t8)
                                        } else {
                                            lbrace(t8).flatMap { t9 ->
                                                // ELSE { ... }
                                                Statements.parseUntil(
                                                    t9,
                                                    headDetector,
                                                    stmtParsers,
                                                    expressionParser,
                                                    isTerminator = { it is SeparatorToken && it.separator == Separator.RBRACE },
                                                ).flatMap { (elseStmts, ta) ->
                                                    rbrace(ta).map { (rElse, tb) ->
                                                        IfStmt(cond, thenStmts, elseStmts, Span(ifKw.span.start, rElse.span.end)) to tb
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun lparen(ts: TokenStream) = expectSeparator(ts, Separator.LPAREN).map { (_, n) -> n }
    private fun rparen(ts: TokenStream) = expectSeparator(ts, Separator.RPAREN).map { (_, n) -> n }
    private fun lbrace(ts: TokenStream) = expectSeparator(ts, Separator.LBRACE).map { (_, n) -> n }
    private fun rbrace(ts: TokenStream) = expectSeparator(ts, Separator.RBRACE)

    private fun maybeElse(ts: TokenStream): Result<Pair<Boolean, TokenStream>, LabeledError> =
        ts.peek().flatMap { t ->
            if (t is KeywordToken && t.kind == Keyword.ELSE) {
                ts.next().map { (_, n) -> true to n }
            } else {
                Success(false to ts)
            }
        }
}
