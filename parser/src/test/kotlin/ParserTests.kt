import dsl.kw
import dsl.op
import dsl.sep
import dsl.ty
import factories.GlobalParserFactory
import head.FirstHeadDetector
import head.Unknown
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParserTests {

    @Test
    fun testParseAssignation() {
        val parser = GlobalParserFactory.forVersion("1.0")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    identifier("x").op().assign().number("42").sep().semicolon()
                },
            ),
        )

        assertTrue(program.statements.size == 1)
        val stmt = program.statements[0]
        assertTrue(stmt is Assignment)
        val assignment = stmt as Assignment
        assertEquals("x", assignment.name)
        assertTrue(assignment.value is LiteralNumber)
        assertEquals("42", (assignment.value as LiteralNumber).raw)
    }

    @Test
    fun testLetWithNoInitializer() {
        val parser = GlobalParserFactory.forVersion("1.0")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().let().identifier("a").sep().colon().ty().numberType().sep().semicolon()
                },
            ),
        )

        val stmt = program.statements[0]
        assertTrue(stmt is VarDeclaration)
        val varDeclaration = stmt as VarDeclaration
        assertEquals("a", varDeclaration.name)
        assertEquals(Type.NUMBER, varDeclaration.type)
        assertNull(varDeclaration.initializer)
    }

    @Test
    fun testParseLetWithInitialization() {
        val parser = GlobalParserFactory.forVersion("1.0")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().let().identifier("b").sep().colon().ty().numberType().op().assign().number("2").op().plus().number("3").sep().semicolon()
                },
            ),
        )

        val stmt = program.statements[0]
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
    }

    @Test
    fun testErrorCausedByMissingSemicolon() {
        val parser = GlobalParserFactory.forVersion("1.0")!!

        val result = parser.parse(
            TestUtils.tokens {
                identifier("x").op().assign().number("42")
                // falta semicolon()
            },
        )

        val error = TestUtils.assertFailure(result)
        assertTrue(error.message.contains("Se esperaba separador SEMICOLON"))
    }

    @Test
    fun testV10DoesNotSupportIf() {
        val parser = GlobalParserFactory.forVersion("1.0")!!

        val result = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("c").sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("a").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val error = TestUtils.assertFailure(result)
    }

    @Test
    fun testV11SupportsIf() {
        val parser = GlobalParserFactory.forVersion("1.1")!!

        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("c").sep().rparen()
                        .sep().lbrace()
                        .kw().println().sep().lparen().string("a").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                },
            ),
        )

        assertEquals(1, program.statements.size)
        val ifStmt = program.statements.single() as IfStmt
        assertTrue(ifStmt.condition is Variable)
        assertEquals("c", (ifStmt.condition as Variable).name)
    }

    @Test
    fun testUnknownHead() {
        val detector = FirstHeadDetector()
        val ts: TokenStream = TestUtils.tokens {
            number("42").sep().semicolon()
        }
        val head = TestUtils.assertSuccess(detector.detect(ts))
        assertEquals(Unknown, head)
    }
}
