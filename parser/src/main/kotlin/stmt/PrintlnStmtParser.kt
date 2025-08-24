package stmt

import Println
import Span
import Statement
import TokenStream
import expr.ExpressionParser

// println(expr) ;
object PrintlnStmtParser : StmtParser {
    override fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement {
        val println = tokenStream.expectKeyword(Keyword.PRINTLN) // println
        tokenStream.expectSep(Separator.LPAREN) // (
        val expression = expr.parseExpression(tokenStream) // expr
        tokenStream.expectSep(Separator.RPAREN) // )
        val semicol = tokenStream.expectSep(Separator.SEMICOLON) // ;
        val span = Span(println.span.start, semicol.span.end)
        return Println(expression, span)
    }
}
