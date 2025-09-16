import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.factories.GlobalFormatterFactory
import org.printscript.token.TestUtils
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import java.io.StringWriter

class FormatterTestV10 {

    @Test
    fun testBasicVariableDeclaration() {
        val formatter = GlobalFormatterFactory.forVersion(Version.V0)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x:number=5;", output.toString())
    }

    @Test
    fun testSpaceAroundAssignmentTrue() {
        val config = FormatterConfig(spaceAroundAssignment = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x:number = 5;", output.toString())
    }

    @Test
    fun testSpaceAroundAssignmentFalse() {
        val config = FormatterConfig(spaceAroundAssignment = false)
        val formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x:number=5;", output.toString())
    }

    @Test
    fun testSpaceBeforeColonInDeclTrue() {
        val config = FormatterConfig(spaceBeforeColonInDecl = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x :number=5;", output.toString())
    }

    @Test
    fun testSpaceBeforeColonInDeclFalse() {
        val config = FormatterConfig(spaceBeforeColonInDecl = false)
        val formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x:number=5;", output.toString())
    }

    @Test
    fun testSpaceAfterColonInDeclTrue() {
        val config = FormatterConfig(spaceAfterColonInDecl = true)
        val formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x: number=5;", output.toString())
    }

    @Test
    fun testSpaceAfterColonInDeclFalse() {
        val config = FormatterConfig(spaceAfterColonInDecl = false)
        val formatter = GlobalFormatterFactory.forVersion(Version.V0, config)!!
        val tokenStream = TestUtils.tokens {
            kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("let x:number=5;", output.toString())
    }

    @Test
    fun testPrintlnStatement() {
        val formatter = GlobalFormatterFactory.forVersion(Version.V0)!!
        val tokenStream = TestUtils.tokens {
            kw().println().sep().lparen().string("Hello World").sep().rparen().sep().semicolon()
        }

        val output = StringWriter()
        formatter.format(tokenStream, output)

        assertEquals("println(\"Hello World\");", output.toString())
    }
}
