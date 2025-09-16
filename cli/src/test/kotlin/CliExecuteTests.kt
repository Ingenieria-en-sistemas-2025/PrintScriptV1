import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliExecuteTests : CliHarness() {

    @Test
    fun `execute prints program output`() {
        val src = file("hello.ps", """println("hi");""").toString()

        val r = run("execute", "-f", src, "-v", "1.1")
        assertEquals(0, r.code)
        assertTrue(r.out.lines().any { it.contains("hi") })
        assertTrue(r.err.isBlank())
    }

    @Test
    fun `execute supports readInput from stdin and shows prompt plus result`() {
        val src = file(
            "ask.ps",
            """
            const name: string = readInput("Name:");
            println("Hello " + name);
            """.trimIndent(),
        ).toString()

        val r = run("execute", "-f", src, "-v", "1.1", stdin = "Ada\n")
        assertEquals(0, r.code)
        // Prompt y saludo deberían salir por stdout (según wiring de printer del CLI)
        assertTrue(r.out.contains("Name:"))
        assertTrue(r.out.contains("Hello Ada"))
        assertTrue(r.err.isBlank())
    }

    @Test
    fun `execute prints error and reports it on stdout when runtime error occurs`() {
        val src = file("boom.ps", """println(1 / 0);""").toString()

        val r = run("execute", "-f", src, "-v", "1.1")

        assertEquals(0, r.code)

        assertTrue(r.out.contains("Execute error"), "Debe reportar el error en stdout")
    }
}
