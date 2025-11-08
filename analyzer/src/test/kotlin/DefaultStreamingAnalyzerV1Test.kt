import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.AnalyzerFactory
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.IdentifiersConfig
import org.printscript.analyzer.config.PrintlnRuleConfig
import org.printscript.analyzer.config.ReadInputRuleConfig
import org.printscript.analyzer.rules.IdentifierStyle
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

    @Test
    fun testWithNoError() {
        // Programa: let x : number=1; println("ok");
        val ast = listOf(
            varDecl(
                name = "x",
                type = Type.NUMBER,
                init = litNum("1", 1, 16),
                l1 = 1,
                c1 = 1,
                l2 = 1,
                c2 = 17,
            ),
            printlnNode(
                expr = litStr("ok", 1, 27),
                l1 = 1,
                c1 = 19,
                l2 = 1,
                c2 = 32,
            ),
        )

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE),
            printlnRule = PrintlnRuleConfig(enabled = true),
            readInputRule = ReadInputRuleConfig(enabled = true),
        )

        val engine = AnalyzerFactory.forVersion(Version.V1)
        val out = CollectorEmitter()

        val res = engine.analyze(streamOf(*ast.toTypedArray()), cfg, out)

        assertTrue(res is Success<Unit>)
        assertEquals(0, out.diags.size)
    }
}
