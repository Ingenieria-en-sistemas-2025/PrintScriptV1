package stmt

import Assignment
import Statement
import TokenStream
import expr.ExpressionParser


// nombre = expr ;
object AssignmentStmtParser : StmtParser {
    override fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement {
        val name = tokenStream.expectIdentifier().identifier
        tokenStream.expectOp(Operator.ASSIGN) // = (obligatorio)
        val value = expr.parseExpression(tokenStream)
        tokenStream.expectSep(Separator.SEMICOLON) // ;\
        return Assignment(name, value)
    }
}