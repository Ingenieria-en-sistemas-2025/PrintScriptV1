import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliExecuteErrorBehaviorTests : CliHarness() {

    @Test
    fun `execute prints runtime error message to stdout (exit 0 by current behavior)`() {
        val src = file("boom.ps", """println(1 / 0);""").toString()

        val r = run("execute", "-f", src, "-v", "1.1")

        assertEquals(0, r.code)
        assertTrue(r.out.contains("Execute error"), "Debe reportar el error en stdout")
    }

    @Test
    fun `execute with readInput lee stdin y no duplica salidas con --collect-also`() {
        val src = file(
            "ask.ps",
            """
            const name: string = readInput("Name:");
            println("Hello " + name);
            """.trimIndent(),
        ).toString()

        val r = run("execute", "-f", src, "-v", "1.1", "--collect-also", stdin = "Ada\n")

        assertEquals(0, r.code)
        val outs = r.out.lines().filter { it.isNotBlank() }
        val nameCount = outs.count { it.contains("Name:") }
        val helloCount = outs.count { it.contains("Hello Ada") }
        assertEquals(1, nameCount, "Prompt duplicado?")
        assertEquals(1, helloCount, "Salida duplicada?")
    }
}
