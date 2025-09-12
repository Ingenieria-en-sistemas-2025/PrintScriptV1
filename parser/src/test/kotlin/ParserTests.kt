import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.Grouping
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralNumber
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Operator
import org.printscript.common.Type
import org.printscript.common.Version
import org.printscript.parser.factories.GlobalParserFactory
import org.printscript.parser.head.FirstHeadDetector
import org.printscript.parser.head.Unknown
import org.printscript.token.TestUtils
import org.printscript.token.TokenStream
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParserTests {

    @Test
    fun testParseAssignation() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
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
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
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
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
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
        val parser = GlobalParserFactory.forVersion(Version.V1)!!

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
        val parser = GlobalParserFactory.forVersion(Version.V0)!!

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
        val parser = GlobalParserFactory.forVersion(Version.V1)!!

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

    @Test
    fun testMultiplyAndDivideLeftAssociativity() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().let().identifier("m").sep().colon().ty().numberType()
                        .op().assign()
                        .number("6").op().divide().number("3").op().minus().number("2")
                        .sep().semicolon()
                },
            ),
        )

        val stmt = program.statements[0]
        assertTrue(stmt is VarDeclaration)
        val varDecl = stmt as VarDeclaration
        assertEquals("m", varDecl.name)
        assertNotNull(varDecl.initializer)

        // (6 / 3) - 2
        val min = varDecl.initializer as Binary
        assertEquals(Operator.MINUS, min.operator)
        assertTrue(min.right is LiteralNumber)
        assertEquals("2", (min.right as LiteralNumber).raw)

        val div = min.left as Binary
        assertEquals(Operator.DIVIDE, div.operator)
        assertTrue(div.left is LiteralNumber)
        assertEquals("6", (div.left as LiteralNumber).raw)
        assertTrue(div.right is LiteralNumber)
        assertEquals("3", (div.right as LiteralNumber).raw)
    }

    @Test
    fun testGroupingParsesToGroupingNode() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().let().identifier("g").sep().colon().ty().numberType()
                        .op().assign()
                        .sep().lparen()
                        .number("2").op().plus().number("3")
                        .sep().rparen()
                        .op().multiply().number("4")
                        .sep().semicolon()
                },
            ),
        )

        val stmt = program.statements[0]
        assertTrue(stmt is VarDeclaration)
        val varDecl = stmt as VarDeclaration
        assertEquals("g", varDecl.name)
        val mul = varDecl.initializer as Binary
        assertEquals(Operator.MULTIPLY, mul.operator)

        assertTrue(mul.left is Grouping)
        val grouping = mul.left as Grouping

        assertTrue(grouping.expression is Binary)
        val plus = grouping.expression as Binary
        assertEquals(Operator.PLUS, plus.operator)
        assertTrue(plus.left is LiteralNumber)
        assertEquals("2", (plus.left as LiteralNumber).raw)
        assertTrue(plus.right is LiteralNumber)
        assertEquals("3", (plus.right as LiteralNumber).raw)
        assertTrue(mul.right is LiteralNumber)
        assertEquals("4", (mul.right as LiteralNumber).raw)
    }

    @Test
    fun testAssertSuccessThrowsOnParseFailure() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val result = parser.parse(
            TestUtils.tokens {
                identifier("x").op().assign().number("42")
            },
        )

        val ex = assertThrows<IllegalStateException> {
            TestUtils.assertSuccess(result)
        }
        val msg = ex.message ?: ""
        assertTrue(msg.contains("Se esperaba separador SEMICOLON"))
    }
}
