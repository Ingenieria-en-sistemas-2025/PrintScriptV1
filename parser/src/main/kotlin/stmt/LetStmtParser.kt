package stmt

import Span
import Statement
import TokenStream
import VarDeclaration
import expr.ExpressionParser

// let nombre: tipo (= expr)? ;
object LetStmtParser : StmtParser {
    override fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement {
        tokenStream.expectKeyword(Keyword.LET) // let
        val name = tokenStream.expectIdentifier() // nombre
        tokenStream.expectSep(Separator.COLON) // :
        val declaredType = tokenStream.expectTypeToken().type // tipo (numb o str)

        // expr
        val initializer =
            if (tokenStream.consumeIfOperator(Operator.ASSIGN)) {
                expr.parseExpression(tokenStream)
            } else {
                null
            }
        val semicol = tokenStream.expectSep(Separator.SEMICOLON) // ;
        val span = Span(name.span.start, semicol.span.end)
        return VarDeclaration(name.identifier, declaredType, initializer, span)
    }
}
