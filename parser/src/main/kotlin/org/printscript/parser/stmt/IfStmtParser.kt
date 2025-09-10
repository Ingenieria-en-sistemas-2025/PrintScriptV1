package org.printscript.parser.stmt

import org.printscript.ast.Expression
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

    private data class Block(val stmts: List<Statement>, val rBrace: SeparatorToken)
    // rBrace sirve para tener el } consumido y su span

    override fun parse(
        ts: TokenStream,
        expressionParser: ExpressionParser,
    ): Result<Pair<Statement, TokenStream>, LabeledError> =
        expectIf(ts).flatMap { (ifKw, t1) ->
            parseCondition(t1, expressionParser).flatMap { (cond, t2) ->
                parseBlock(t2, expressionParser).flatMap { (thenB, t3) ->
                    maybeElse(t3).flatMap { (hasElse, t4) ->
                        if (!hasElse) {
                            finishNoElse(ifKw, cond, thenB, t4)
                        } else {
                            finishWithElse(ifKw, cond, thenB, t4, expressionParser)
                        }
                    }
                }
            }
        }
    private fun expectIf(ts: TokenStream) = expectKeyword(ts, Keyword.IF)

    private fun parseCondition(
        ts: TokenStream,
        p: ExpressionParser,
    ) = lparen(ts).flatMap { t2 ->
        p.parseExpression(t2).flatMap { (cond, t3) ->
            rparen(t3).map { (_, t4) -> cond to t4 }
        }
    }

    private fun parseBlock(
        ts: TokenStream,
        p: ExpressionParser,
    ) = lbrace(ts).flatMap { t1 ->
        Statements.parseUntil(
            t1,
            headDetector,
            stmtParsers,
            p,
            isTerminator = { it is SeparatorToken && it.separator == Separator.RBRACE },
        ).flatMap { (stmts, t2) ->
            rbrace(t2).map { (r, t3) -> Block(stmts, r) to t3 }
        }
    }

    private fun finishNoElse(
        ifKw: KeywordToken,
        cond: Expression,
        thenB: Block,
        next: TokenStream,
    ): Result<Pair<Statement, TokenStream>, LabeledError> =
        Success(IfStmt(cond, thenB.stmts, null, Span(ifKw.span.start, thenB.rBrace.span.end)) to next)

    private fun finishWithElse(
        ifKw: KeywordToken,
        cond: Expression,
        thenB: Block,
        afterElseKw: TokenStream,
        p: ExpressionParser,
    ) = parseBlock(afterElseKw, p).map { (elseB, endTs) ->
        IfStmt(cond, thenB.stmts, elseB.stmts, Span(ifKw.span.start, elseB.rBrace.span.end)) to endTs
    }

    private fun lparen(ts: TokenStream) = expectSeparator(ts, Separator.LPAREN).map { (_, n) -> n }
    private fun rparen(ts: TokenStream) = expectSeparator(ts, Separator.RPAREN).map { it }
    private fun lbrace(ts: TokenStream) = expectSeparator(ts, Separator.LBRACE).map { (_, n) -> n }
    private fun rbrace(ts: TokenStream) = expectSeparator(ts, Separator.RBRACE)

    private fun maybeElse(ts: TokenStream) =
        ts.peek().flatMap { t ->
            if (t is KeywordToken && t.kind == Keyword.ELSE) {
                ts.next().map { (_, n) -> true to n }
            } else {
                Success(false to ts)
            }
        }
}
