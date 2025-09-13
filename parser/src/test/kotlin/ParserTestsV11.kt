import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.printscript.ast.Binary
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ReadInput
import org.printscript.ast.Step
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Operator
import org.printscript.common.Type
import org.printscript.common.Version
import org.printscript.parser.factories.GlobalParserFactory
import org.printscript.token.TestUtils
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty

class ParserTestsV11 {
    @Test
    fun testConstDeclarationWithNumber() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().const().identifier("PI").sep().colon().ty().numberType()
                    .op().assign().number("3.14").sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is ConstDeclaration)
        val constDecl = stmt as ConstDeclaration
        assertEquals("PI", constDecl.name)
        assertEquals(Type.NUMBER, constDecl.type)
        assertTrue(constDecl.initializer is LiteralNumber)
        assertEquals("3.14", (constDecl.initializer as LiteralNumber).raw)
    }

    @Test
    fun testConstDeclarationWithString() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().const().identifier("name").sep().colon().ty().stringType()
                    .op().assign().string("Hello").sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is ConstDeclaration)
        val constDecl = stmt as ConstDeclaration
        assertEquals("name", constDecl.name)
        assertEquals(Type.STRING, constDecl.type)
        assertTrue(constDecl.initializer is LiteralString)
        assertEquals("Hello", (constDecl.initializer as LiteralString).value)
    }

    @Test
    fun testConstMissingInitializerError() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().const().identifier("x").sep().colon().ty().stringType().sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Error)
        val error = (step as Step.Error).error
        assertTrue(
            error.message.contains("Se esperaba operador ASSIGN") ||
                error.message.contains("ASSIGN"),
        )
    }

    @Test
    fun testLetWithReadInputInitializer() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().let().identifier("name").sep().colon().ty().stringType()
                    .op().assign()
                    .kw().readInput().sep().lparen().string("Enter name: ").sep().rparen()
                    .sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is VarDeclaration)
        val varDecl = stmt as VarDeclaration
        assertEquals("name", varDecl.name)
        assertEquals(Type.STRING, varDecl.type)
        assertTrue(varDecl.initializer is ReadInput)
        val readInput = varDecl.initializer as ReadInput
        assertTrue(readInput.prompt is LiteralString)
        assertEquals("Enter name: ", (readInput.prompt as LiteralString).value)
    }

    @Test
    fun testConcatenationWithReadInput() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().println().sep().lparen()
                    .string("Hello ").op().plus()
                    .kw().readInput().sep().lparen().string("name: ").sep().rparen()
                    .sep().rparen().sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is Println)
        val println = stmt as Println
        assertTrue(println.value is Binary)
        val binary = println.value as Binary
        assertEquals(Operator.PLUS, binary.operator)
        assertTrue(binary.left is LiteralString)
        assertEquals("Hello ", (binary.left as LiteralString).value)
        assertTrue(binary.right is ReadInput)
    }

    @Test
    fun testIfWithEmptyBlocks() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("x").sep().rparen()
                    .sep().lbrace().sep().rbrace()
                    .kw().elsekey()
                    .sep().lbrace().sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is IfStmt)
        val ifStmt = stmt as IfStmt
        assertEquals(0, ifStmt.thenBranch.size)
        assertNotNull(ifStmt.elseBranch)
        assertEquals(0, ifStmt.elseBranch!!.size)
    }

    @Test
    fun testIfWithComplexCondition() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen()
                    .identifier("x").op().plus().identifier("y")
                    .sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("sum").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is IfStmt)
        val ifStmt = stmt as IfStmt
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
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("flag").sep().rparen()
                    .sep().lbrace()
                    .kw().let().identifier("x").sep().colon().ty().numberType().sep().semicolon()
                    .kw().println().sep().lparen().string("hello").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is IfStmt)
        val ifStmt = stmt as IfStmt
        assertEquals(2, ifStmt.thenBranch.size)

        val letStmt = ifStmt.thenBranch[0]
        assertTrue(letStmt is VarDeclaration)
        assertEquals("x", (letStmt as VarDeclaration).name)

        val printStmt = ifStmt.thenBranch[1]
        assertTrue(printStmt is Println)
    }

    @Test
    fun testNestedIf() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("outer").sep().rparen()
                    .sep().lbrace()
                    .kw().ifkey().sep().lparen().identifier("inner").sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("nested").sep().rparen().sep().semicolon()
                    .sep().rbrace()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Item)
        val stmt = (step as Step.Item).statement
        assertTrue(stmt is IfStmt)
        val outerIf = stmt as IfStmt
        assertEquals("outer", (outerIf.condition as Variable).name)
        assertEquals(1, outerIf.thenBranch.size)

        val innerIf = outerIf.thenBranch[0] as IfStmt
        assertEquals("inner", (innerIf.condition as Variable).name)
        assertEquals(1, innerIf.thenBranch.size)

        val println = innerIf.thenBranch[0] as Println
        assertEquals("nested", (println.value as LiteralString).value)
    }

    @Test
    fun testIfMissingConditionError() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().sep().rparen()
                    .sep().lbrace()
                    .kw().println().sep().lparen().string("test").sep().rparen().sep().semicolon()
                    .sep().rbrace()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Error)
    }

    @Test
    fun testIfMissingBracesError() {
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val statementStream = parser.parse(
            TestUtils.tokens {
                kw().ifkey().sep().lparen().identifier("x").sep().rparen()
                    .kw().println().sep().lparen().string("test").sep().rparen().sep().semicolon()
            },
        )

        val step = statementStream.nextStep()
        assertTrue(step is Step.Error)
        val error = (step as Step.Error).error
        assertTrue(error.message.contains("LBRACE") || error.message.contains("llave"))
    }
}
