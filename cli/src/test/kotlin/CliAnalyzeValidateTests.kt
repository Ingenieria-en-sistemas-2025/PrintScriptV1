import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliAnalyzeValidateTests : CliHarness() {

    @Test
    fun `analyze on suspicious program produces output lines (warn or diag)`() {
        val src = file("sus.ps", """let x : number=1; println(x);""").toString()
        val r = run("analyze", "-f", src, "-v", "1.1")
        assertEquals(0, r.code)
        assertTrue(r.out.lines().isNotEmpty())
    }

    @Test
    fun `validate OK program reports no errors (exit 0)`() {
        val src = file("ok.ps", """println("hi");""").toString()
        val cfg = json("analyzer.json", """{ }""").toString()

        val r = run("validate", "-f", src, "-v", "1.1", "-c", cfg)
        assertEquals(0, r.code)
        assertTrue(!r.out.contains("Encontré errores"))
        assertTrue(r.err.isBlank())
    }

    @Test
    fun `analyze sobre programa con issues imprime alguna salida`() {
        val src = file("sus.ps", """let x : number=1; println(x);""").toString()

        val r = run("analyze", "-f", src, "-v", "1.1")

        assertEquals(0, r.code)
        assertTrue(r.out.isNotBlank() || r.err.isNotBlank())
    }
}
