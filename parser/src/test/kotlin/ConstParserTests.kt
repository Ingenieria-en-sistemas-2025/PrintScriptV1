import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.ast.Binary
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.Variable
import org.printscript.common.Operator
import org.printscript.common.Type
import org.printscript.parser.factories.GlobalParserFactory
import org.printscript.token.TestUtils
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstParserTests {

    @Test
    fun testConstWithInitializer_Number() {
        // const c: number = 10;
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().const().identifier("c").sep().colon().ty().numberType().op().assign().number("10").sep().semicolon()
                },
            ),
        )

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0]
        assertTrue(stmt is ConstDeclaration)
        val c = stmt as ConstDeclaration
        assertEquals("c", c.name)
        assertEquals(Type.NUMBER, c.type)
        assertTrue(c.initializer is LiteralNumber)
        assertEquals("10", (c.initializer as LiteralNumber).raw)
    }

    @Test
    fun testConstWithInitializer_String() {
        // const name: string = "Hello";
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().const().identifier("name").sep().colon().ty().stringType().op().assign().string("Hello").sep().semicolon()
                },
            ),
        )

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0]
        assertTrue(stmt is ConstDeclaration)
        val c = stmt as ConstDeclaration
        assertEquals("name", c.name)
        assertEquals(Type.STRING, c.type)
        assertTrue(c.initializer is LiteralString)
        assertEquals("Hello", (c.initializer as LiteralString).value)
    }

    @Test
    fun testConstWithExpression() {
        // const result: number = 5 + 3;
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().const().identifier("result").sep().colon().ty().numberType().op().assign().number("5").op().plus().number("3")
                        .sep().semicolon()
                },
            ),
        )

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0]
        assertTrue(stmt is ConstDeclaration)
        val c = stmt as ConstDeclaration
        assertEquals("result", c.name)
        assertEquals(Type.NUMBER, c.type)
        assertTrue(c.initializer is Binary)
        val binary = c.initializer as Binary
        assertEquals(Operator.PLUS, binary.operator)
        assertTrue(binary.left is LiteralNumber)
        assertEquals("5", (binary.left as LiteralNumber).raw)
        assertTrue(binary.right is LiteralNumber)
        assertEquals("3", (binary.right as LiteralNumber).raw)
    }

    @Test
    fun testConstMissingInitializer_Fails() {
        // const x: string;  (falta '=' expr)
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val result = parser.parse(
            TestUtils.tokens {
                kw().const().identifier("x").sep().colon().ty().stringType().sep().semicolon()
            },
        )

        val error = TestUtils.assertFailure(result)
        assertTrue(
            error.message.contains("Se esperaba operador ASSIGN") ||
                error.message.contains("ASSIGN"),
        )
    }

    @Test
    fun testConstMissingType_Fails() {
        // const x = "value";  (falta tipo)
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val result = parser.parse(
            TestUtils.tokens {
                kw().const().identifier("x").op().assign().string("value").sep().semicolon()
            },
        )

        val error = TestUtils.assertFailure(result)
        assertTrue(
            error.message.contains("Se esperaba separador COLON") ||
                error.message.contains("COLON"),
        )
    }

    @Test
    fun testProgramWithConst_AndPrintln() {
        // const name: string = "Milagros"; println(name);
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().const().identifier("name").sep().colon().ty().stringType().op().assign().string("Milagros").sep().semicolon()
                    kw().println().sep().lparen().identifier("name").sep().rparen().sep().semicolon()
                },
            ),
        )

        assertEquals(2, program.statements.size)

        val constStmt = program.statements[0]
        assertTrue(constStmt is ConstDeclaration)
        val constDecl = constStmt as ConstDeclaration
        assertEquals("name", constDecl.name)
        assertEquals(Type.STRING, constDecl.type)
        assertTrue(constDecl.initializer is LiteralString)
        assertEquals("Milagros", (constDecl.initializer as LiteralString).value)

        val printStmt = program.statements[1]
        assertTrue(printStmt is Println)
        val pr = printStmt as Println
        assertTrue(pr.value is Variable)
        assertEquals("name", (pr.value as Variable).name)
    }

    @Test
    fun testMultipleConstDeclarations() {
        // const x: number = 42; const y: string = "test";
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().const().identifier("x").sep().colon().ty().numberType().op().assign().number("42").sep().semicolon()
                    kw().const().identifier("y").sep().colon().ty().stringType().op().assign().string("test").sep().semicolon()
                },
            ),
        )

        assertEquals(2, program.statements.size)

        val firstConst = program.statements[0] as ConstDeclaration
        assertEquals("x", firstConst.name)
        assertEquals(Type.NUMBER, firstConst.type)

        val secondConst = program.statements[1] as ConstDeclaration
        assertEquals("y", secondConst.name)
        assertEquals(Type.STRING, secondConst.type)
    }
}
