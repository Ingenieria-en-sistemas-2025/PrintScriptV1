import dsl.kw
import dsl.op
import dsl.sep
import dsl.ty
import factories.GlobalParserFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReadsParserTests {
    // let name: string = readInput("Nombre: ");
    @Test
    fun testLetWithReadInputInitializer() {
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().let().identifier("name").sep().colon().ty().stringType()
                        .op().assign()
                        .kw().readInput().sep().lparen().string("Nombre: ").sep().rparen()
                        .sep().semicolon()
                },
            ),
        )

        val stmt = program.statements.single() as VarDeclaration
        assertEquals("name", stmt.name)
        assertEquals(Type.STRING, stmt.type)
        assertNotNull(stmt.initializer)
        assertTrue(stmt.initializer is ReadInput)
        val ri = stmt.initializer as ReadInput
        assertTrue(ri.prompt is LiteralString)
        assertEquals("Nombre: ", (ri.prompt as LiteralString).value)
    }

    // println(readEnv("HOME"));
    @Test
    fun testPrintlnWithReadEnv() {
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().println().sep().lparen()
                        .kw().readEnv().sep().lparen().string("HOME").sep().rparen()
                        .sep().rparen().sep().semicolon()
                },
            ),
        )

        val stmt = program.statements.single() as Println
        assertTrue(stmt.value is ReadEnv)
        val re = stmt.value as ReadEnv
        assertTrue(re.variableName is LiteralString)
        assertEquals("HOME", (re.variableName as LiteralString).value)
    }

    // println("Hola " + readInput("nombre:"));
    @Test
    fun testConcatWithReadInputInsideExpression() {
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().println().sep().lparen()
                        .string("Hola ").op().plus()
                        .kw().readInput().sep().lparen().string("nombre:").sep().rparen()
                        .sep().rparen().sep().semicolon()
                },
            ),
        )

        val stmt = program.statements.single() as Println
        val bin = stmt.value as Binary
        assertTrue(bin.left is LiteralString)
        assertEquals("Hola ", (bin.left as LiteralString).value)
        assertTrue(bin.right is ReadInput)
        val ri = bin.right as ReadInput
        assertTrue(ri.prompt is LiteralString)
        assertEquals("nombre:", (ri.prompt as LiteralString).value)
    }

    @Test
    fun testReadInputMissingRParen() {
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val result = parser.parse(
            TestUtils.tokens {
                kw().println().sep().lparen()
                    .kw().readInput().sep().lparen().string("x")
                // falta rparen() del readInput, falta rparen() del println y falta semicolon()
            },
        )
        val error = TestUtils.assertFailure(result)
        assertTrue(error.message.contains("RPAREN"))
    }
}
