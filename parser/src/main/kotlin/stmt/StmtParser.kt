package stmt

import ast.Statement
import parser.TokenStream

interface StmtParser {
    fun parse(tokenStream: TokenStream, expr: ExpressionParser) : Statement
}