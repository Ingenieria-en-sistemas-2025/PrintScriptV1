import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliFormatTests : CliHarness() {

    @Test
    fun `format prints formatted code to stdout (default config)`() {
        val src = file("prog.ps", """println("hi");""").toString()

        val r = run("format", "-f", src, "-v", "1.1")
        assertEquals(0, r.code)
        assertTrue(r.out.contains("println"))
        assertTrue(r.out.isNotBlank())
        assertTrue(r.err.isBlank())
    }

    @Test
    fun `format respects config path and --indent override (JSON)`() {
        val src = file("prog.ps", """if(true){println(1+2);}else{println(3+4);}""").toString()
        val cfg = json("formatter.json", """{ "indentSpaces": 2 }""").toString()

        val r = run("format", "-f", src, "-v", "1.1", "-c", cfg, "--indent", "4")
        assertEquals(0, r.code)
        assertTrue(r.out.contains("    println(1 + 2);")) // 4 espacios
        assertTrue(r.err.isBlank())
    }

    @Test
    fun `format accepts YAML config too`() {
        val src = file("prog.ps", """if(true){println(1+2);}""").toString()
        val cfg = yaml("formatter.yaml", "indentSpaces: 2").toString()

        val r = run("format", "-f", src, "-v", "1.1", "-c", cfg)
        assertEquals(0, r.code)
        assertTrue(r.out.contains("  println(1 + 2);")) // 2 espacios
        assertTrue(r.err.isBlank())
    }
}
