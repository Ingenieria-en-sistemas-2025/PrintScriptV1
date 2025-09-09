import dsl.kw
import dsl.op
import dsl.sep
import dsl.ty
import factories.GlobalParserFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IfParserTests {

    @Test
    fun testIfThen_NoElse() {
        // if (c) { println("a"); }
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
        assertEquals(1, ifStmt.thenBranch.size)
        assertNull(ifStmt.elseBranch)
        assertTrue(ifStmt.thenBranch[0] is Println)
        val pr = ifStmt.thenBranch[0] as Println
        assertTrue(pr.value is LiteralString)
        assertEquals("a", (pr.value as LiteralString).value)
    }

    @Test
    fun testIfThenElse() {
        // if (c) { println("a"); } else { println("b"); }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("c").sep().rparen()
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

        assertEquals(1, program.statements.size)
        val ifStmt = program.statements.single() as IfStmt
        assertEquals(1, ifStmt.thenBranch.size)
        assertNotNull(ifStmt.elseBranch)
        val elseBranch = ifStmt.elseBranch!!
        assertEquals(1, elseBranch.size)
        val prElse = elseBranch[0] as Println
        assertTrue(prElse.value is LiteralString)
        assertEquals("b", (prElse.value as LiteralString).value)
    }

    @Test
    fun testIfEmptyBlocks() {
        // if (x) { } else { }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("x").sep().rparen()
                        .sep().lbrace().sep().rbrace()
                        .kw().elsekey()
                        .sep().lbrace().sep().rbrace()
                },
            ),
        )

        val ifStmt = program.statements.single() as IfStmt
        assertEquals(0, ifStmt.thenBranch.size)
        assertNotNull(ifStmt.elseBranch)
        assertEquals(0, ifStmt.elseBranch!!.size)
    }

    @Test
    fun testIfWithComplexCondition() {
        // if (x + y) { println("sum"); }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("x").op().plus().identifier("y").sep().rparen()
                        .sep().lbrace()
                        .kw().println().sep().lparen().string("sum").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                },
            ),
        )

        val ifStmt = program.statements.single() as IfStmt
        assertTrue(ifStmt.condition is Binary)
        val condition = ifStmt.condition as Binary
        assertEquals(Operator.PLUS, condition.operator)
        assertTrue(condition.left is Variable)
        assertEquals("x", (condition.left as Variable).name)
        assertTrue(condition.right is Variable)
        assertEquals("y", (condition.right as Variable).name)
    }

    @Test
    fun testIfWithMultipleStatements() {
        // if (flag) { let x: number; println("hello"); }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("flag").sep().rparen()
                        .sep().lbrace()
                        .kw().let().identifier("x").sep().colon().ty().numberType().sep().semicolon()
                        .kw().println().sep().lparen().string("hello").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                },
            ),
        )

        val ifStmt = program.statements.single() as IfStmt
        assertEquals(2, ifStmt.thenBranch.size)

        val letStmt = ifStmt.thenBranch[0]
        assertTrue(letStmt is VarDeclaration)
        assertEquals("x", (letStmt as VarDeclaration).name)

        val printStmt = ifStmt.thenBranch[1]
        assertTrue(printStmt is Println)
        val pr = printStmt as Println
        assertTrue(pr.value is LiteralString)
        assertEquals("hello", (pr.value as LiteralString).value)
    }

    @Test
    fun testIfWithAssignment() {
        // if (condition) { x = 42; } else { x = 0; }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("condition").sep().rparen()
                        .sep().lbrace()
                        .identifier("x").op().assign().number("42").sep().semicolon()
                        .sep().rbrace()
                        .kw().elsekey()
                        .sep().lbrace()
                        .identifier("x").op().assign().number("0").sep().semicolon()
                        .sep().rbrace()
                },
            ),
        )

        val ifStmt = program.statements.single() as IfStmt
        val thenAssign = ifStmt.thenBranch[0] as Assignment
        assertEquals("x", thenAssign.name)
        assertTrue(thenAssign.value is LiteralNumber)
        assertEquals("42", (thenAssign.value as LiteralNumber).raw)

        assertNotNull(ifStmt.elseBranch)
        val elseAssign = ifStmt.elseBranch!![0] as Assignment
        assertEquals("x", elseAssign.name)
        assertTrue(elseAssign.value is LiteralNumber)
        assertEquals("0", (elseAssign.value as LiteralNumber).raw)
    }

    @Test
    fun testNestedIf() {
        // if (outer) { if (inner) { println("hello"); } }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val program = TestUtils.assertSuccess(
            parser.parse(
                TestUtils.tokens {
                    kw().ifkey().sep().lparen().identifier("outer").sep().rparen()
                        .sep().lbrace()
                        .kw().ifkey().sep().lparen().identifier("inner").sep().rparen()
                        .sep().lbrace()
                        .kw().println().sep().lparen().string("hello").sep().rparen().sep().semicolon()
                        .sep().rbrace()
                        .sep().rbrace()
                },
            ),
        )

        val outerIf = program.statements.single() as IfStmt
        assertEquals("outer", (outerIf.condition as Variable).name)
        assertEquals(1, outerIf.thenBranch.size)

        val innerIf = outerIf.thenBranch[0] as IfStmt
        assertEquals("inner", (innerIf.condition as Variable).name)
        assertEquals(1, innerIf.thenBranch.size)

        val println = innerIf.thenBranch[0] as Println
        assertEquals("hello", (println.value as LiteralString).value)
    }

    @Test
    fun testIfMissingCondition_Fails() {
        // if () { println("test"); }
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val result = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("test").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val error = TestUtils.assertFailure(result)
        assertTrue(error.message.isNotBlank())
    }

    @Test
    fun testIfMissingBraces_Fails() {
        // if (x) println("test"); faltan llaves
        val parser = GlobalParserFactory.forVersion("1.1")!!
        val result = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("x").sep().rparen()
                    .kw().println().sep().lparen().string("test").sep().rparen().sep().semicolon()
            },
        )

        val error = TestUtils.assertFailure(result)
        assertTrue(error.message.contains("LBRACE") || error.message.contains("llave"))
    }

    @Test
    fun testV10DoesNotSupportIf_ComparisonTest() {
        val v10Parser = GlobalParserFactory.forVersion("1.0")!!
        val v11Parser = GlobalParserFactory.forVersion("1.1")!!

        val tokens = TestUtils.tokens {
            kw().ifkey().sep().lparen().identifier("c").sep().rparen()
                .sep().lbrace()
                .kw().println().sep().lparen().string("a").sep().rparen().sep().semicolon()
                .sep().rbrace()
        }

        val v10Result = v10Parser.parse(tokens)
        TestUtils.assertFailure(v10Result)

        val v11Result = v11Parser.parse(tokens)
        val program = TestUtils.assertSuccess(v11Result)
        assertEquals(1, program.statements.size)
        assertTrue(program.statements[0] is IfStmt)
    }
}
