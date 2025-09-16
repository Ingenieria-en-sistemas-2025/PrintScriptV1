

import org.junit.jupiter.api.Test
import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.Severity
import org.printscript.common.Position
import org.printscript.common.Span
import org.printscript.runner.helpers.DiagnosticStringFormatter
import kotlin.test.assertEquals

class DiagnosticStringFormatterTest {

    @Test
    fun `format with span prints coordinates`() {
        val d = Diagnostic(
            ruleId = "PS-ID-STYLE",
            message = "Identifier 'my_variable' no respeta convención",
            span = Span(
                start = Position(line = 2, column = 5),
                end = Position(line = 2, column = 30),
            ),
            severity = Severity.WARNING,
        )
        val s = DiagnosticStringFormatter.format(d)
        assertEquals(
            "[WARNING][PS-ID-STYLE] Identifier 'my_variable' no respeta convención @ L2:C5-L2:C30",
            s,
        )
    }

    @Test
    fun `spanToString null prints placeholder via reflection`() {
        // spanToString(Span?) es private; lo invocamos con null para cubrir la rama placeholder
        val cls = DiagnosticStringFormatter::class.java
        val m = cls.getDeclaredMethod("spanToString", Span::class.java)
        m.isAccessible = true
        val res = m.invoke(DiagnosticStringFormatter, *arrayOf<Any?>(null)) as String
        assertEquals("(?:?:?-?:?)", res)
    }

    @Test
    fun `pos null prints L question marks via reflection`() {
        // pos(Position?) también es private; cubrimos L?:C?
        val cls = DiagnosticStringFormatter::class.java
        val m = cls.getDeclaredMethod("pos", Position::class.java)
        m.isAccessible = true
        val res = m.invoke(DiagnosticStringFormatter, *arrayOf<Any?>(null)) as String
        assertEquals("L?:C?", res)
    }
}
