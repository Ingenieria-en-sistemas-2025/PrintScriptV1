import expr.RecursiveExpressionParser
import head.FirstHeadDetector
import head.HeadDetector
import head.Kw
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import stmt.AssignmentStmtParser
import stmt.LetStmtParser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParserTests {

    private val exprParser = RecursiveExpressionParser()
    private val detector: HeadDetector = FirstHeadDetector()

    @Test
    fun testParseAssignation() {
        val result = AssignmentStmtParser.parse(
            tsOf(
                IdentifierToken("x", Span(Position(1, 1), Position(1, 1))),
                OperatorToken(Operator.ASSIGN, Span(Position(1, 3), Position(1, 3))),
                NumberLiteralToken("42", Span(Position(1, 5), Position(1, 6))),
                SeparatorToken(Separator.SEMICOLON, Span(Position(1, 7), Position(1, 7))),
            ),
            exprParser,
        )
        val (stmt, tsNext) = assertSuccess(result)
        assertTrue(stmt is Assignment)
        val assignment = stmt as Assignment
        assertEquals("x", assignment.name)
        assertTrue(assignment.value is LiteralNumber)
        assertEquals("42", (assignment.value as LiteralNumber).raw)
        assertTrue(tsNext.isEof())
    }

    @Test
    fun testLetWithNoInitializer() {
        val result = LetStmtParser.parse(
            tsOf(
                KeywordToken(Keyword.LET, Span(Position(1, 1), Position(1, 3))),
                IdentifierToken("a", Span(Position(1, 5), Position(1, 5))),
                SeparatorToken(Separator.COLON, Span(Position(1, 6), Position(1, 6))),
                TypeToken(Type.NUMBER, Span(Position(1, 8), Position(1, 12))),
                SeparatorToken(Separator.SEMICOLON, Span(Position(1, 13), Position(1, 13))),
            ),
            exprParser,
        )
        val (stmt, tsNext) = assertSuccess(result)
        assertTrue(stmt is VarDeclaration)
        val varDeclaration = stmt as VarDeclaration
        assertEquals("a", varDeclaration.name)
        assertEquals(Type.NUMBER, varDeclaration.type)
        assertNull(varDeclaration.initializer)
        assertTrue(tsNext.isEof())
    }

    @Test
    fun testParseLetWithInitialization() {
        // let b: number = 2 + 3;
        val result = LetStmtParser.parse(
            tsOf(
                KeywordToken(Keyword.LET, Span(Position(1, 1), Position(1, 3))),
                IdentifierToken("b", Span(Position(1, 5), Position(1, 5))),
                SeparatorToken(Separator.COLON, Span(Position(1, 6), Position(1, 6))),
                TypeToken(Type.NUMBER, Span(Position(1, 8), Position(1, 12))),
                OperatorToken(Operator.ASSIGN, Span(Position(1, 14), Position(1, 14))),
                NumberLiteralToken("2", Span(Position(1, 16), Position(1, 16))),
                OperatorToken(Operator.PLUS, Span(Position(1, 18), Position(1, 18))),
                NumberLiteralToken("3", Span(Position(1, 20), Position(1, 20))),
                SeparatorToken(Separator.SEMICOLON, Span(Position(1, 21), Position(1, 21))),
            ),
            exprParser,
        )
        val (stmt, tsNext) = assertSuccess(result)
        assertTrue(stmt is VarDeclaration)
        val varDeclaration = stmt as VarDeclaration
        assertEquals("b", varDeclaration.name)
        assertEquals(Type.NUMBER, varDeclaration.type)
        assertNotNull(varDeclaration.initializer)
        val binary = varDeclaration.initializer as Binary
        assertEquals(Operator.PLUS, binary.operator)
        assertTrue(binary.left is LiteralNumber)
        assertEquals("2", (binary.left as LiteralNumber).raw)
        assertTrue(binary.right is LiteralNumber)
        assertEquals("3", (binary.right as LiteralNumber).raw)
        assertTrue(tsNext.isEof())
    }

    @Test
    fun testPrintLnWithoutExpression() {
        val parser = FirstParser()
        val program = assertSuccess(
            parser.parse(
                tsOf(
                    KeywordToken(Keyword.PRINTLN, Span(Position(1, 1), Position(1, 7))),
                    SeparatorToken(Separator.LPAREN, Span(Position(1, 8), Position(1, 8))),
                    StringLiteralToken("hola", Span(Position(1, 9), Position(1, 14))),
                    SeparatorToken(Separator.RPAREN, Span(Position(1, 15), Position(1, 15))),
                    SeparatorToken(Separator.SEMICOLON, Span(Position(1, 16), Position(1, 16))),
                ),
            ),
        )
        assertEquals(1, program.statements.size)
        val stmt = program.statements.single()
        assertTrue(stmt is Println)
        val printlnStmt = stmt as Println
        assertTrue(printlnStmt.value is LiteralString)
        assertEquals("hola", (printlnStmt.value as LiteralString).value)
    }

    @Test
    fun testPrintlnWithExpression() {
        val parser = FirstParser()
        val program = assertSuccess(
            parser.parse(
                tsOf(
                    KeywordToken(Keyword.PRINTLN, Span(Position(1, 1), Position(1, 7))),
                    SeparatorToken(Separator.LPAREN, Span(Position(1, 8), Position(1, 8))),
                    IdentifierToken("x", Span(Position(1, 9), Position(1, 9))),
                    SeparatorToken(Separator.RPAREN, Span(Position(1, 10), Position(1, 10))),
                    SeparatorToken(Separator.SEMICOLON, Span(Position(1, 11), Position(1, 11))),
                ),
            ),
        )
        val stmt = program.statements.single()
        assertTrue(stmt is Println)
        val printlnStmt = stmt as Println
        assertTrue(printlnStmt.value is Variable)
        assertEquals("x", (printlnStmt.value as Variable).name)
    }

    @Test
    fun testHeadDetector() {
        val ts = tsOf(KeywordToken(Keyword.LET, Span(Position(1, 10), Position(1, 10))))
        val headResult = detector.detect(ts)
        val head = assertSuccess(headResult)
        assertEquals(Kw(Keyword.LET), head)

        val first = assertSuccess(ts.peek())
        assertTrue(first is KeywordToken && first.kind == Keyword.LET)
    }

    @Test
    fun testErrorCausedByMissingSemicolon() {
        val parser = FirstParser()
        val result = parser.parse(
            tsOf(
                IdentifierToken("x", Span(Position(1, 1), Position(1, 1))),
                OperatorToken(Operator.ASSIGN, Span(Position(1, 3), Position(1, 3))),
                NumberLiteralToken("42", Span(Position(1, 5), Position(1, 6))),
                // falta ';' antes de EOF
            ),
        )
        when (result) {
            is Success -> error("Esperaba Failure, obtuve Success")
            is Failure -> {
                val error = result.error
                assertTrue(error is ParserError)
                assertTrue(error.message.contains("Se esperaba separador SEMICOLON"))
                assertEquals(Position(1, 5), error.span.start)
            }
        }
    }

    // Agrega un EOF (obligatorio) y construye el stream inmutable
    private fun tsOf(vararg toks: Token): TokenStream {
        val lastSpan = toks.lastOrNull()?.span ?: Span(Position(1, 1), Position(1, 1))
        val eof = EofToken(lastSpan)
        return ListTokenStream.of((toks.toList() + eof))
    }

    // Desempaqueta un Success o falla el test con el error legible
    private fun <T> assertSuccess(res: Result<T, LabeledError>): T =
        res.fold(
            onSuccess = { it },
            onFailure = { error("Esperaba Success, obtuve Failure: ${it.humanReadable()}") },
        )
}
