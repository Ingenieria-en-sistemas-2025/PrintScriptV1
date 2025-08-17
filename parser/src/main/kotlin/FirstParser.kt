import expr.ExpressionParser
import head.Assign
import head.Head
import head.HeadDetector
import head.Kw
import stmt.AssignmentStmtParser
import stmt.LetStmtParser
import stmt.PrintlnStmtParser
import stmt.StmtParser


class FirstParser(
    private val headDetector: HeadDetector,
    private val expr: ExpressionParser,
    private val stmtParsers: Map<Head, StmtParser> = mapOf(
        Kw(Keyword.LET) to LetStmtParser,
        Kw(Keyword.PRINTLN) to PrintlnStmtParser,
        Assign to AssignmentStmtParser
    )
) : Parser {

    override fun parse(tokenStream: TokenStream): ProgramNode {
        val out = mutableListOf<Statement>()
        while (!tokenStream.isAtEnd()) {
            out.add(parseStatement(tokenStream))
        }
        return ProgramNode(out)
    }

    private fun parseStatement(tokenStream: TokenStream): Statement {
        val head = headDetector.detect(tokenStream)
        val stmtParser = stmtParsers[head]
            ?: error("Inicio de sentencia no reconocido: ${tokenStream.peek()}")
        return stmtParser.parse(tokenStream, expr)
    }
}
