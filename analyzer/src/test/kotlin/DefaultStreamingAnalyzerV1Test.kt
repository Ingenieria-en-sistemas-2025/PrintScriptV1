import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.AnalyzerConfig
import org.printscript.analyzer.AnalyzerFactory
import org.printscript.analyzer.IdentifierStyle
import org.printscript.analyzer.IdentifiersConfig
import org.printscript.analyzer.PrintlnRuleConfig
import org.printscript.analyzer.ReadInputRuleConfig
import org.printscript.common.Operator
import org.printscript.common.Success
import org.printscript.common.Type
import org.printscript.common.Version
import kotlin.test.Test

class DefaultStreamingAnalyzerV1Test {

    @Test
    fun readInputConExpresionCompuesta_reportaError() {
        // let x: string = readInput("A" + "B");  -> debe fallar por arg compuesto
        val init = readInputExpr(
            binary(litStr("A", 1, 25), Operator.PLUS, litStr("B", 1, 31), 1, 25, 1, 32),
            1,
            20,
            1,
            32,
        )
        val s1 = varDecl("x", Type.STRING, init, 1, 1, 1, 33)

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE),
            printlnRule = PrintlnRuleConfig(enabled = true),
            readInputRule = ReadInputRuleConfig(enabled = true, onlyStringLiteralOrIdentifier = false),
        )

        val engine = AnalyzerFactory.forVersion(Version.V1)

        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1), cfg, out)
        assertTrue(res is Success<Unit>)
        assertEquals(1, out.diags.size)
        assertEquals("PS-READINPUT-SIMPLE", out.diags.first().ruleId)
    }
}
