import expr.ExpressionParser
import head.Head
import head.HeadDetector
import stmt.StmtParser

class FirstParser(
    private val headDetector: HeadDetector,
    private val expr: ExpressionParser,
    private val stmtParsers: Map<Head, StmtParser>,
) : Parser {

    override fun parse(ts0: TokenStream): Result<ProgramNode, LabeledError> =
        Statements.parseUntil(
            ts0,
            headDetector,
            stmtParsers,
            expr,
            isTerminator = { false }, // nunca cortamos por token
            stopAtEof = true, // cortamos por EOF
        ).map { (stmts, _) -> ProgramNode(stmts) }
}
