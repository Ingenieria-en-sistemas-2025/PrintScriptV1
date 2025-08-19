package stmt

import Statement
import TokenStream
import VarDeclaration
import expr.ExpressionParser


// let nombre: tipo (= expr)? ;
object LetStmtParser : StmtParser {
    override fun parse(tokenStream: TokenStream, expr: ExpressionParser): Statement {
        tokenStream.expectKeyword(Keyword.LET) // let
        val name = tokenStream.expectIdentifier().identifier // nombre
        tokenStream.expectSep(Separator.COLON) // :
        val declaredType = tokenStream.expectTypeToken().type // tipo (numb o str)

        // expr
        val initializer =
            if (tokenStream.consumeIfOperator(Operator.ASSIGN)) expr.parseExpression(tokenStream)
            else null
        tokenStream.expectSep(Separator.SEMICOLON) // ;
        return VarDeclaration(name, declaredType, initializer)
    }

}