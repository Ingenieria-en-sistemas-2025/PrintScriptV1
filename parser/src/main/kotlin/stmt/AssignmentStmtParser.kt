package stmt

import Assignment
import Span
import Statement
import TokenStream
import expr.ExpressionParser

// nombre = expr ;
object AssignmentStmtParser : StmtParser {
    override fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement {
        val name = tokenStream.expectIdentifier()
        tokenStream.expectOp(Operator.ASSIGN) // = (obligatorio)
        val value = expr.parseExpression(tokenStream)
        val semicol = tokenStream.expectSep(Separator.SEMICOLON) // ;\
        val span = Span(name.span.start, semicol.span.end)
        return Assignment(name.identifier, value, span)
    }
}
