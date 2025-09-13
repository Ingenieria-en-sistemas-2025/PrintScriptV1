import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.Grouping
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ReadEnv
import org.printscript.ast.ReadInput
import org.printscript.ast.Step
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Operator
import org.printscript.common.Success
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

class ParserTests {

    @Test
    fun testParseAssignment() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                identifier("x").op().assign().number("42").sep().semicolon()
            },
        )
        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is Assignment)
        val assignment = stmt as Assignment
        assertEquals("x", assignment.name)
        assertTrue(assignment.value is LiteralNumber)
        assertEquals("42", (assignment.value as LiteralNumber).raw)
    }

    @Test
    fun testLetWithNoInitializer() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("a").sep().colon().ty().numberType().sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is VarDeclaration)
        val varDeclaration = stmt as VarDeclaration
        assertEquals("a", varDeclaration.name)
        assertEquals(Type.NUMBER, varDeclaration.type)
        assertNull(varDeclaration.initializer)
    }

    @Test
    fun testParseLetWithInitialization() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("b").sep().colon().ty().numberType().op().assign().number("2").op().plus().number("3").sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
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
    fun testConstDeclaration() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().const().identifier("PI").sep().colon().ty().numberType().op().assign().number("3.14").sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is ConstDeclaration)
        val constDeclaration = stmt as ConstDeclaration
        assertEquals("PI", constDeclaration.name)
        assertEquals(Type.NUMBER, constDeclaration.type)
        assertTrue(constDeclaration.initializer is LiteralNumber)
        assertEquals("3.14", (constDeclaration.initializer as LiteralNumber).raw)
    }

    @Test
    fun testPrintlnStatement() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().println().sep().lparen().string("hello").sep().rparen().sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is Println)
        val printlnStmt = stmt as Println
        assertTrue(printlnStmt.value is LiteralString)
        assertEquals("hello", (printlnStmt.value as LiteralString).value)
    }

    @Test
    fun testErrorCausedByMissingSemicolon() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                identifier("x").op().assign().number("42")
                // falta semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Error)
        val error = (step as Step.Error).error
        assertTrue(error.message.contains("Se esperaba separador SEMICOLON"))
    }

    @Test
    fun testV0DoesNotSupportIf() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("c").sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("a").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Error)
    }

    @Test
    fun testV1SupportsIf() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("c").sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("a").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is IfStmt)
        val ifStmt = stmt as IfStmt
        assertTrue(ifStmt.condition is Variable)
        assertEquals("c", (ifStmt.condition as Variable).name)
    }

    @Test
    fun testIfElseStatement() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().boolean(true).sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("true").sep().rparen().sep().semicolon()
                    .sep().rbrace()
                    .kw().elsekey()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("false").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is IfStmt)
        val ifStmt = stmt as IfStmt
        assertTrue(ifStmt.condition is LiteralBoolean)
        assertNotNull(ifStmt.elseBranch)
    }

    @Test
    fun testReadInputInV1() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                identifier("name").op().assign().kw().readInput().sep().lparen().string("Enter name: ").sep().rparen().sep().semicolon()
            },
        )
        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is Assignment)
        val assignment = stmt as Assignment
        assertTrue(assignment.value is ReadInput)
    }

    @Test
    fun testReadEnvInV1() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                identifier("path").op().assign().kw().readEnv().sep().lparen().string("PATH").sep().rparen().sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is Assignment)
        val assignment = stmt as Assignment
        assertTrue(assignment.value is ReadEnv)
    }

    @Test
    fun testUnknownHead() {
        val detector = FirstHeadDetector()
        val ts: TokenStream = TestUtils.tokens {
            number("42").sep().semicolon()
        }
        val result = detector.detect(ts)
        assertTrue(result is Success)
        assertEquals(Unknown, (result as Success).value)
    }

    @Test
    fun testMultiplyAndDivideLeftAssociativity() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("m").sep().colon().ty().numberType()
                    .op().assign()
                    .number("6").op().divide().number("3").op().minus().number("2")
                    .sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
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
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("g").sep().colon().ty().numberType()
                    .op().assign()
                    .sep().lparen()
                    .number("2").op().plus().number("3")
                    .sep().rparen()
                    .op().multiply().number("4")
                    .sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
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
    fun testBooleanLiteralInV1() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                identifier("flag").op().assign().boolean(true).sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is Assignment)
        val assignment = stmt as Assignment
        assertTrue(assignment.value is LiteralBoolean)
        assertEquals(true, (assignment.value as LiteralBoolean).value)
    }

    @Test
    fun testStringTypeDeclaration() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("message").sep().colon().ty().stringType().op().assign().string("Hello").sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is VarDeclaration)
        val varDeclaration = stmt as VarDeclaration
        assertEquals("message", varDeclaration.name)
        assertEquals(Type.STRING, varDeclaration.type)
        assertTrue(varDeclaration.initializer is LiteralString)
        assertEquals("Hello", (varDeclaration.initializer as LiteralString).value)
    }

    @Test
    fun testBooleanTypeDeclarationV1() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("isActive").sep().colon().ty().booleanType().op().assign().boolean(false).sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is VarDeclaration)
        val varDeclaration = stmt as VarDeclaration
        assertEquals("isActive", varDeclaration.name)
        assertEquals(Type.BOOLEAN, varDeclaration.type)
        assertTrue(varDeclaration.initializer is LiteralBoolean)
        assertEquals(false, (varDeclaration.initializer as LiteralBoolean).value)
    }

    @Test
    fun testMultipleStatements() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("x").sep().colon().ty().numberType().op().assign().number("5").sep().semicolon()
                kw().println().sep().lparen().identifier("x").sep().rparen().sep().semicolon()
            },
        )
        // 1er statement
        val step1 = statementStream.nextStep()
        assertTrue(step1 is Step.Item)
        assertTrue((step1 as Step.Item).statement is VarDeclaration)
        // 2 statement
        val step2 = step1.next.nextStep()
        assertTrue(step2 is Step.Item)
        assertTrue((step2 as Step.Item).statement is Println)
        // EOF
        val step3 = step2.next.nextStep()
        assertTrue(step3 is Step.Eof)
    }

    @Test
    fun testErrorRecovery() {
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                identifier("invalid")
                kw().println().sep().lparen().string("recovered").sep().rparen().sep().semicolon()
            },
        )
        // Primer step tiene que ser error
        val step1 = statementStream.nextStep()
        assertTrue(step1 is Step.Error)
        // Se tiene que recuperar
        val step2 = (step1 as Step.Error).next.nextStep()
        assertTrue(step2 is Step.Item)
        assertTrue((step2 as Step.Item).statement is Println)
    }
}
