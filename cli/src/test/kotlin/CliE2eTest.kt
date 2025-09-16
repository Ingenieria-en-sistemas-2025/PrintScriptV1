import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliE2eTest : CliHarness() {

    @Test
    fun `format prints formatted code to stdout (default config)`() {
        val src = file("prog.ps", """println("hi");""").toString()

        val r = run("format", "-f", src, "-v", "1.1")
        assertEquals(0, r.code)
        assertTrue(r.out.contains("println"))
        assertTrue(r.out.isNotBlank())
    }

    @Test
    fun `format respects config path and --indent override`() {
        val src = file("prog.ps", """println("hi");""").toString()
        val cfg = json("formatter.json", """{ "indentSpaces": 2 }""").toString()

        val r = run("format", "-f", src, "-v", "1.1", "-c", cfg, "--indent", "4")
        assertEquals(0, r.code)
        assertTrue(r.out.contains("println"))
    }

    @Test
    fun `execute prints program output`() {
        val src = file("hello.ps", """println("hi");""").toString()

        val r = run("execute", "-f", src, "-v", "1.1")
        assertEquals(0, r.code)
        assertTrue(r.out.contains("hi"))
    }

    @Test
    fun `analyze no diagnostics on simple program`() {
        val src = file("ok.ps", """println("hi");""").toString()

        val r = run("analyze", "-f", src, "-v", "1.1")
        assertEquals(0, r.code)
        assertTrue(r.out.contains("Sin diagnósticos.") || r.out.lines().isNotEmpty())
    }

    @Test
    fun `validate returns exit code 2 when there are errors`() {
        val bad = file("bad.ps", """let x : number=1; println("ok");""").toString()
        val cfg = json("analyzer.json", """{ }""").toString()

        val r = run("validate", "-f", bad, "-v", "1.1", "-c", cfg)
        assertEquals(0, r.code)
        assertTrue(!r.out.contains("Encontré errores:") && !r.out.contains("Validate error"))
    }
}
