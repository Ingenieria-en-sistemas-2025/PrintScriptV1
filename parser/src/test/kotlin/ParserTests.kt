import expr.RecursiveExpressionParser
import head.FirstHeadDetector
import head.Head
import head.HeadDetector
import head.Kw
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import stmt.AssignmentStmtParser
import stmt.LetStmtParser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParserTests {

    private val exprParser = RecursiveExpressionParser()
    private val detector: HeadDetector = FirstHeadDetector()

    @Test
    fun testParseAssignation() {
        val tokens: List<Token> = listOf(
            IdentifierToken("x", Span(Position(1, 1), Position(1, 1))),
            OperatorToken(Operator.ASSIGN, Span(Position(1, 3), Position(1, 3))),
            NumberLiteralToken("42", Span(Position(1, 5), Position(1, 6))),
            SeparatorToken(Separator.SEMICOLON, Span(Position(1, 7), Position(1, 7))),
        )
        val ts = TokenStream(tokens)
        val stmt = AssignmentStmtParser.parse(ts, exprParser)

        assertTrue(stmt is Assignment)
        val a = stmt as Assignment
        assertEquals("x", a.name)
        assertTrue(a.value is LiteralNumber)
        assertEquals("42", (a.value as LiteralNumber).raw)
    }

    @Test
    fun testLetWithNoInitializer() {
        val tokens: List<Token> = listOf(
            KeywordToken(Keyword.LET, Span(Position(1, 1), Position(1, 3))),
            IdentifierToken("a", Span(Position(1, 5), Position(1, 5))),
            SeparatorToken(Separator.COLON, Span(Position(1, 6), Position(1, 6))),
            TypeToken(Type.NUMBER, Span(Position(1, 8), Position(1, 12))),
            SeparatorToken(Separator.SEMICOLON, Span(Position(1, 13), Position(1, 13))),
        )

        val ts = TokenStream(tokens)
        val stmt = LetStmtParser.parse(ts, exprParser)

        assertTrue(stmt is VarDeclaration)
        val v = stmt as VarDeclaration
        assertEquals("a", v.name)
        assertEquals(Type.NUMBER, v.type)
        assertNull(v.initializer)
        assertTrue(ts.isAtEnd())
    }

    @Test
    fun testParseLetWithInitialization() {
        // let b number = 2 + 3
        val tokens: List<Token> = listOf(
            KeywordToken(Keyword.LET, Span(Position(1, 1), Position(1, 3))),
            IdentifierToken("b", Span(Position(1, 5), Position(1, 5))),
            SeparatorToken(Separator.COLON, Span(Position(1, 6), Position(1, 6))),
            TypeToken(Type.NUMBER, Span(Position(1, 8), Position(1, 12))),
            OperatorToken(Operator.ASSIGN, Span(Position(1, 14), Position(1, 14))),
            NumberLiteralToken("2", Span(Position(1, 16), Position(1, 16))),
            OperatorToken(Operator.PLUS, Span(Position(1, 18), Position(1, 18))),
            NumberLiteralToken("3", Span(Position(1, 20), Position(1, 20))),
            SeparatorToken(Separator.SEMICOLON, Span(Position(1, 21), Position(1, 21))),
        )

        val ts = TokenStream(tokens)
        val stmt = LetStmtParser.parse(ts, exprParser)

        assertTrue(stmt is VarDeclaration)
        val v = stmt as VarDeclaration
        assertEquals("b", v.name)
        assertEquals(Type.NUMBER, v.type)
        assertNotNull(v.initializer)

        val init = v.initializer!!
        assertTrue(init is Binary)
        val bin = init as Binary
        assertEquals(Operator.PLUS, bin.operator)
        assertTrue(bin.left is LiteralNumber)
        assertEquals("2", (bin.left as LiteralNumber).raw)
        assertTrue(bin.right is LiteralNumber)
        assertEquals("3", (bin.right as LiteralNumber).raw)
        assertTrue(ts.isAtEnd())
    }

    @Test
    fun testPrintLnWithoutExpression() {
        val tokens: List<Token> = listOf(
            KeywordToken(Keyword.PRINTLN, Span(Position(1, 1), Position(1, 7))),
            SeparatorToken(Separator.LPAREN, Span(Position(1, 8), Position(1, 8))),
            StringLiteralToken("hola", Span(Position(1, 9), Position(1, 14))),
            SeparatorToken(Separator.RPAREN, Span(Position(1, 15), Position(1, 15))),
            SeparatorToken(Separator.SEMICOLON, Span(Position(1, 16), Position(1, 16))),
        )
        val parser = FirstParser()
        val program = parser.parse(TokenStream(tokens))

        assertEquals(1, program.statements.size)
        val stmt = program.statements.single()
        assertTrue(stmt is Println)
        val p = stmt as Println
        assertTrue(p.value is LiteralString)
        assertEquals("hola", (p.value as LiteralString).value)
    }

    @Test
    fun testPrintlnWithExpression() {
        val tokens: List<Token> = listOf(
            KeywordToken(Keyword.PRINTLN, Span(Position(1, 1), Position(1, 7))),
            SeparatorToken(Separator.LPAREN, Span(Position(1, 8), Position(1, 8))),
            IdentifierToken("x", Span(Position(1, 9), Position(1, 9))),
            SeparatorToken(Separator.RPAREN, Span(Position(1, 10), Position(1, 10))),
            SeparatorToken(Separator.SEMICOLON, Span(Position(1, 11), Position(1, 11))),
        )
        val parser = FirstParser()
        val program = parser.parse(TokenStream(tokens))

        assertEquals(1, program.statements.size)
        val stmt = program.statements.single()
        assertTrue(stmt is Println)
        val p = stmt as Println
        assertTrue(p.value is Variable)
        assertEquals("x", (p.value as Variable).name)
    }

    @Test
    fun testHeadDetector() {
        val tokens: List<Token> = listOf(
            KeywordToken(Keyword.LET, Span(Position(1, 10), Position(1, 10))),
        )
        val ts = TokenStream(tokens)

        val head: Head = detector.detect(ts)
        assertEquals(Kw(Keyword.LET), head)

        val first = ts.peek()
        assertTrue(first is KeywordToken && first.kind == Keyword.LET)
    }

    @Test
    fun testErrorCausedByMissingSemicolon() {
        val tokens: List<Token> = listOf(
            IdentifierToken("x", Span(Position(1, 1), Position(1, 1))),
            OperatorToken(Operator.ASSIGN, Span(Position(1, 3), Position(1, 3))),
            NumberLiteralToken("42", Span(Position(1, 5), Position(1, 6))),
            // falta ;
        )

        val ts = TokenStream(tokens)
        val parser = FirstParser()

        val ex = assertThrows(ParseError::class.java) {
            parser.parse(ts)
        }

        assertTrue(ex.message!!.contains("Se esperaba separador SEMICOLON"))
        assertEquals(Position(1, 6), ex.span.start)
    }
}
