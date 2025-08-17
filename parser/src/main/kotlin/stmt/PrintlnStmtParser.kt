package stmt

import ast.Statement
import parser.TokenStream

// println(expr) ;
object PrintlnStmtParser : StmtParser {
    override fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement {
        tokenStream.expectKeyword(Keyword.PRINTLN) // println
        tokenStream.expectSep(Separator.LPAREN) // (
        val expression = expr.parseExpression(tokenStream) // expr
        tokenStream.expectSep(Separator.RPAREN) // )
        tokenStream.expectSep(Separator.SEMICOLON) // ;
        return Println(expression)
    }
}