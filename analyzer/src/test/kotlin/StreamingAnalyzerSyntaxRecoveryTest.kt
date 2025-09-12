import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.AnalyzerConfig
import org.printscript.analyzer.DefaultStreamingAnalyzer
import org.printscript.analyzer.PrintlnRuleConfig
import org.printscript.analyzer.PrintlnSimpleArgRuleStreaming
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Position
import org.printscript.common.Span
import org.printscript.common.Success
import kotlin.test.Test

class StreamingAnalyzerSyntaxRecoveryTest {

    @Test
    fun anteErrorDeParser_emitePSSYNTAX_ySigueConNext() {
        // Simulamos: primero un error de parser, luego println(1+2) que viola "simple"
        val err = LabeledError.of(
            Span(Position(10, 5), Position(10, 6)),
            "Token inesperado",
        )
        val after = listOf(
            printlnNode(
                binary(litNum("1", 11, 10), Operator.PLUS, litNum("2", 11, 14), 11, 10, 11, 15),
                11,
                1,
                11,
                16,
            ),
        )

        val cfg = AnalyzerConfig(printlnRule = PrintlnRuleConfig(enabled = true))
        val engine = DefaultStreamingAnalyzer(listOf(PrintlnSimpleArgRuleStreaming()))

        val out = CollectorEmitter()
        val res = engine.analyze(streamWithError(err, after), cfg, out)
        assertTrue(res is Success<Unit>)

        val diags = out.diags.sortedWith(compareBy({ it.span.start.line }, { it.span.start.column }))
        assertEquals(2, diags.size)
        assertEquals("PS-SYNTAX", diags[0].ruleId)
        assertEquals(10, diags[0].span.start.line)
        assertEquals("PS-PRINTLN-SIMPLE", diags[1].ruleId)
        assertEquals(11, diags[1].span.start.line)
    }
}
