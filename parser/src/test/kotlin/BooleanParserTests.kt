import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.ast.Assignment
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.common.Type
import org.printscript.common.Version
import org.printscript.parser.factories.GlobalParserFactory
import org.printscript.token.TestUtils
import org.printscript.token.TokenStream
import org.printscript.token.TypeToken
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BooleanParserTests {

    @Test
    fun testPrintlnTrue() {
        // println(true);
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().println()
                        .sep().lparen()
                        .boolean(true)
                        .sep().rparen()
                        .sep().semicolon()
                },
            ),
        )
        val pr = program.statements.single() as Println
        assertTrue(pr.value is LiteralBoolean)
        assertEquals(true, (pr.value as LiteralBoolean).value)
    }

    @Test
    fun testPrintlnFalse() {
        // println(false);
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().println()
                        .sep().lparen()
                        .boolean(false)
                        .sep().rparen()
                        .sep().semicolon()
                },
            ),
        )
        val pr = program.statements.single() as Println
        assertTrue(pr.value is LiteralBoolean)
        assertEquals(false, (pr.value as LiteralBoolean).value)
    }

    @Test
    fun testAssignmentBooleanTrue() {
        // x = true;
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    identifier("x")
                        .op().assign()
                        .boolean(true)
                        .sep().semicolon()
                },
            ),
        )
        val assign = program.statements.single() as Assignment
        assertEquals("x", assign.name)
        assertTrue(assign.value is LiteralBoolean)
        assertEquals(true, (assign.value as LiteralBoolean).value)
    }

    @Test
    fun testIfWithBooleanLiteralTrue() {
        // if (true) { println("ok"); }
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey()
                        .sep().lparen().boolean(true).sep().rparen()
                        .sep().lbrace()
                        .kw().println().sep().lparen().string("ok").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                },
            ),
        )
        val ifStmt = program.statements.single() as IfStmt
        assertTrue(ifStmt.condition is LiteralBoolean)
        assertEquals(true, (ifStmt.condition as LiteralBoolean).value)
    }

    @Test
    fun testIfFalseElseBranch() {
        // if (false) { println("a"); } else { println("b"); }
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey()
                        .sep().lparen().boolean(false).sep().rparen()
                        .sep().lbrace()
                        .kw().println().sep().lparen().string("a").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                        .kw().elsekey()
                        .sep().lbrace()
                        .kw().println().sep().lparen().string("b").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                },
            ),
        )
        val ifStmt = program.statements.single() as IfStmt
        assertNotNull(ifStmt.elseBranch)
        val elsePr = ifStmt.elseBranch!!.single() as Println
        assertEquals("b", (elsePr.value as LiteralString).value)
    }

    @Test
    fun testBooleanTypeAfterLet() {
        // let b: boolean = true;
        val ts: TokenStream = TestUtils.tokens {
            kw().let()
                .identifier("b")
                .sep().colon()
                .ty().booleanType()
                .op().assign()
                .boolean(true)
                .sep().semicolon()
        }

        // Consumimos 3 tokens: 'let' 'b' ':'
        var s = ts
        repeat(3) { s = TestUtils.assertSuccess(s.next()).second }

        // El siguiente debe ser el TypeToken(Boolean)
        val (tok4, _) = TestUtils.assertSuccess(s.next())
        assertTrue(tok4 is TypeToken)
        assertEquals(Type.BOOLEAN, (tok4 as TypeToken).type)
    }
}
