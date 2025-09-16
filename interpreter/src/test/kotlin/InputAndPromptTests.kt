import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.common.Operator
import org.printscript.common.Success
import org.printscript.common.Type
import org.printscript.common.Version
import kotlin.test.Test
import kotlin.test.assertEquals

class InputAndPromptTests {

    @Test
    fun `readInput without printer collects prompt into Output`() {
        val decl = org.printscript.ast.ConstDeclaration(
            name = "name",
            type = Type.STRING,
            initializer = readInputExpr(litStr("Name:", 1, 20), 1, 1, 1, 28),
            span = span(1, 1, 1, 40),
        )
        val hello = binary(
            binary(litStr("Hello ", 2, 1), Operator.PLUS, variable("name", 2, 11), 2, 1, 2, 22),
            Operator.PLUS,
            litStr("!", 2, 23),
            2,
            1,
            2,
            26,
        )
        val pr = printlnNode(hello, 2, 1, 2, 30)

        val res = run(streamOf(decl, pr), version = Version.V1, input = ListInputProvider(listOf("world")))
        assertTrue(res is Success)
        assertEquals(listOf("Name:", "Hello world!"), (res as Success).value.outputs)
    }

    @Test
    fun `readInput with printer does NOT collect prompt unless collectAlso=true`() {
        val printer = PrinterProbe()
        val decl = org.printscript.ast.ConstDeclaration(
            name = "name",
            type = Type.STRING,
            initializer = readInputExpr(litStr("Name:", 1, 20), 1, 1, 1, 28),
            span = span(1, 1, 1, 40),
        )
        val hello = binary(
            binary(litStr("Hello ", 2, 1), Operator.PLUS, variable("name", 2, 11), 2, 1, 2, 22),
            Operator.PLUS,
            litStr("!", 2, 23),
            2,
            1,
            2,
            26,
        )
        val pr = printlnNode(hello, 2, 1, 2, 30)

        val res = run(streamOf(decl, pr), input = ListInputProvider(listOf("world")), printer = printer, collectAlso = false)
        assertTrue(res is Success)
        assertEquals(listOf("Name:"), (res as Success).value.outputs)
        assertEquals(listOf("Name:", "Hello world!"), printer.lines)
    }

    @Test
    fun `readInput with printer AND collectAlso=true collects prompt too`() {
        val printer = PrinterProbe()
        val decl = org.printscript.ast.ConstDeclaration(
            name = "name",
            type = Type.STRING,
            initializer = readInputExpr(litStr("Name:", 1, 20), 1, 1, 1, 28),
            span = span(1, 1, 1, 40),
        )
        val hello = binary(
            binary(litStr("Hello ", 2, 1), Operator.PLUS, variable("name", 2, 11), 2, 1, 2, 22),
            Operator.PLUS,
            litStr("!", 2, 23),
            2,
            1,
            2,
            26,
        )
        val pr = printlnNode(hello, 2, 1, 2, 30)

        val res = run(streamOf(decl, pr), input = ListInputProvider(listOf("world")), printer = printer, collectAlso = true)
        assertTrue(res is Success)
        assertEquals(listOf("Name:", "Hello world!"), (res as Success).value.outputs)
        assertEquals(listOf("Name:", "Hello world!"), printer.lines)
    }

    @Test
    fun `assignment with readInput collects prompt without printer`() {
        val d = varDecl("name", Type.STRING, readInputExpr(litStr("Name:", 1, 20), 1, 1, 1, 30), 1, 1, 1, 35)
        val p = printlnNode(variable("name", 2, 1), 2, 1, 2, 15)
        val res = run(streamOf(d, p), input = ListInputProvider(listOf("Cati")))
        assertTrue(res is Success)
        assertEquals(listOf("Name:", "Cati"), (res as Success).value.outputs)
    }

    @Test
    fun `assignment with readInput and printer does not collect prompt`() {
        val printer = PrinterProbe()
        val d = varDecl("name", Type.STRING, readInputExpr(litStr("Name:", 1, 20), 1, 1, 1, 30), 1, 1, 1, 35)
        val p = printlnNode(variable("name", 2, 1), 2, 1, 2, 15)

        val res = run(streamOf(d, p), input = ListInputProvider(listOf("Cati")), printer = printer, collectAlso = false)
        assertTrue(res is Success)
        assertEquals(listOf("Name:"), (res as Success).value.outputs)
        assertEquals(listOf("Name:", "Cati"), printer.lines)
    }
}
