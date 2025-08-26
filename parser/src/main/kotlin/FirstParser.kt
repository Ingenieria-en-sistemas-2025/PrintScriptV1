import expr.ExpressionParser
import expr.RecursiveExpressionParser
import head.Assign
import head.FirstHeadDetector
import head.Head
import head.HeadDetector
import head.Kw
import stmt.AssignmentStmtParser
import stmt.LetStmtParser
import stmt.PrintlnStmtParser
import stmt.StmtParser

class FirstParser(
    private val headDetector: HeadDetector = FirstHeadDetector(),
    private val expr: ExpressionParser = RecursiveExpressionParser(),
    private val stmtParsers: Map<Head, StmtParser> = mapOf(
        Kw(Keyword.LET) to LetStmtParser,
        Kw(Keyword.PRINTLN) to PrintlnStmtParser,
        Assign to AssignmentStmtParser,
    ),
) : Parser {

    override fun parse(ts0: TokenStream): Result<ProgramNode, LabeledError> =
        parseProgram(ts0, emptyList())

    // Acumula sentencias
    private fun parseProgram(
        ts: TokenStream,
        accumulator: List<Statement> = emptyList(),
    ): Result<ProgramNode, LabeledError> =
        if (ts.isEof()) { // llegue al final
            Success(ProgramNode(accumulator)) // se construye AST con todas las sentencias acumuladas
        } else {
            parseOne(ts).flatMap { (stmt, nextTs) ->
                parseProgram(nextTs, accumulator + stmt) // accumulator + stmt crea una nueva lista
            }
        }

    // Parsear una sentencia segun el head
    private fun parseOne(ts: TokenStream): Result<Pair<Statement, TokenStream>, LabeledError> =
        headDetector.detect(ts).flatMap { head ->
            val parser = stmtParsers[head]
            if (parser != null) {
                parser.parse(ts, expr)
            } else {
                Failure(ParserError(peekSpan(ts), "Inicio de sentencia no reconocido"))
            }
        }

    private fun peekSpan(ts: TokenStream): Span =
        ts.peek().fold(
            onSuccess = { it.span }, // si peek devuelve el token, usamos su span
            onFailure = { it.span }, // si devuelve error, reutilizamos su span
        )
}
