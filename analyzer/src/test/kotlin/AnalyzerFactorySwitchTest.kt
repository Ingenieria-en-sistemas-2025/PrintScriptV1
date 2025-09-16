import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.AnalyzerFactory
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.IdentifiersConfig
import org.printscript.analyzer.config.PrintlnRuleConfig
import org.printscript.analyzer.config.ReadInputRuleConfig
import org.printscript.common.Success
import org.printscript.common.Version
import kotlin.test.Test

class AnalyzerFactorySwitchTest {
    @Test
    fun readInputReglaAtivaEnV1yNoenV0() {
        // readInput con arg compuesto -> en V1 debería diagnosticar; en V0 no.
        val init = readInputExpr(
            binary(litStr("A", 1, 25), org.printscript.common.Operator.PLUS, litStr("B", 1, 31), 1, 25, 1, 32),
            1,
            20,
            1,
            32,
        )
        val s1 = varDecl("x", org.printscript.common.Type.STRING, init, 1, 1, 1, 33)

        val cfgOn = AnalyzerConfig(
            identifiers = IdentifiersConfig(), // por defecto enabled = true
            printlnRule = PrintlnRuleConfig(enabled = true),
            readInputRule = ReadInputRuleConfig(enabled = true, onlyStringLiteralOrIdentifier = false),
        )

        // V1 -> 1 diagnóstico
        run {
            val engine = AnalyzerFactory.forVersion(Version.V1)
            val out = CollectorEmitter()
            val res = engine.analyze(streamOf(s1), cfgOn, out)
            assertTrue(res is Success<Unit>)
            assertEquals(1, out.diags.size)
            assertEquals("PS-READINPUT-SIMPLE", out.diags.first().ruleId)
        }

        // V0 -> 0 diagnósticos (la regla de ReadInput no está)
        run {
            val engine = AnalyzerFactory.forVersion(Version.V0)
            val out = CollectorEmitter()
            val res = engine.analyze(streamOf(s1), cfgOn, out)
            assertTrue(res is Success<Unit>)
            assertEquals(0, out.diags.size)
        }
    }

    @Test
    fun configDesactivaReglasYNoHayDiagnosticos() {
        val init = readInputExpr(
            binary(litStr("A", 1, 25), org.printscript.common.Operator.PLUS, litStr("B", 1, 31), 1, 25, 1, 32),
            1,
            20,
            1,
            32,
        )
        val s1 = varDecl("x", org.printscript.common.Type.STRING, init, 1, 1, 1, 33)

        val cfgOff = AnalyzerConfig(
            identifiers = IdentifiersConfig(enabled = false),
            printlnRule = PrintlnRuleConfig(enabled = false),
            readInputRule = ReadInputRuleConfig(enabled = false),
        )

        val engine = AnalyzerFactory.forVersion(Version.V1, cfgOff) // usa overload con cfg
        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1), cfgOff, out)
        assertTrue(res is Success<Unit>)
        assertEquals(0, out.diags.size)
    }
}
