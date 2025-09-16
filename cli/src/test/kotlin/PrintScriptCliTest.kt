

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import org.printscript.cli.PrintScriptCli
import org.printscript.cli.commands.AnalyzeCmd
import org.printscript.cli.commands.ExecuteCmd
import org.printscript.cli.commands.FormatCmd
import org.printscript.cli.commands.ValidateCmd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun appWithSubcommands(): PrintScriptCli =
    PrintScriptCli().subcommands(
        ValidateCmd(),
        ExecuteCmd(),
        FormatCmd(),
        AnalyzeCmd(),
    )

class PrintScriptCliTest {

    @Test
    fun `root --help shows subcommands`() {
        val app = appWithSubcommands()
        val r = app.test(arrayOf("--help"))
        assertEquals(0, r.statusCode)
        val out = r.output.lowercase()
        // ahora sí deben estar listados
        assertTrue(out.contains("execute") || out.contains("run"))
        assertTrue(out.contains("format"))
        assertTrue(out.contains("validate"))
        assertTrue(out.contains("analyze"))
    }

    @Test
    fun `no args prints usage or nothing (current behavior)`() {
        val app = appWithSubcommands()
        val r = app.test(emptyArray())

        // status puede ser 0 o no, según config actual del root
        // lo importante: que no explote y que, si imprime algo, sea "usage/help"
        val combined = (r.output + r.stderr).lowercase()

        // pasa si no imprime nada o si imprime usage/help en stdout/stderr
        assertTrue(
            combined.isBlank() ||
                combined.contains("usage") ||
                combined.contains("help") ||
                combined.contains("uso"),
        )
    }

    @Test
    fun `subcommand analyze --help works`() {
        val app = appWithSubcommands()
        val r = app.test(arrayOf("analyze", "--help"))
        assertEquals(0, r.statusCode)
        assertTrue(r.output.isNotBlank())
    }

    @Test
    fun `unknown option at root returns error`() {
        val app = appWithSubcommands()
        val r = app.test(arrayOf("--definitivamente-no-existe"))
        assertTrue(r.statusCode != 0)
        val text = (r.output + r.stderr).lowercase()
        assertTrue(
            text.contains("no such option") ||
                text.contains("unknown option") ||
                text.contains("opción"),
        )
    }
}
