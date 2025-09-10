import org.printscript.analyzer.AnalyzerConfigLoader
import org.printscript.analyzer.IdentifierStyle
import org.printscript.common.Failure
import org.printscript.common.Success
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AnalyzerConfigLoaderTest {

    @Test
    fun cargaYmlOk() {
        val tmp: File = createTempFile(suffix = ".yml").toFile()
        tmp.writeText(
            """
            identifiers:
              style: SNAKE_CASE
              checkReferences: true
              failOnViolation: true
            printlnRule:
              enabled: false
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.SNAKE_CASE, cfg.identifiers.style)
                assertTrue(cfg.identifiers.checkReferences)
                assertTrue(cfg.identifiers.failOnViolation)
                assertFalse(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No pudo leer YAML: ${r.error.message}")
        }

        tmp.delete()
    }

    @Test
    fun cargaJsonOk() {
        val tmp: File = createTempFile(suffix = ".json").toFile()
        tmp.writeText(
            """
            {
              "identifiers": { "style": "CAMEL_CASE", "checkReferences": false },
              "printlnRule": { "enabled": true }
            }
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.CAMEL_CASE, cfg.identifiers.style)
                assertFalse(cfg.identifiers.checkReferences)
                assertTrue(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No pudo leer JSON: ${r.error.message}")
        }

        tmp.delete()
    }

    @Test
    fun defaultsCuandoFaltanCampos() {
        val tmp: File = createTempFile(suffix = ".yml").toFile()
        tmp.writeText(
            """
            identifiers: { }   # todo default (CAMEL_CASE, false, false, null)
            printlnRule: { }    # default enabled=true
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.CAMEL_CASE, cfg.identifiers.style)
                assertFalse(cfg.identifiers.checkReferences)
                assertFalse(cfg.identifiers.failOnViolation)
                assertNull(cfg.identifiers.customRegex)
                assertTrue(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No debería fallar: ${r.error.message}")
        }

        tmp.delete()
    }

    @Test
    fun archivoInexistenteRetornaLabeledError() {
        val missing = File("no-existe-${System.currentTimeMillis()}.yml")
        when (val r = AnalyzerConfigLoader.fromFile(missing)) {
            is Success -> fail("Debió fallar por archivo inexistente")
            is Failure -> {
                assertTrue(r.error.message.contains("Error al leer archivo"))
                assertEquals(1, r.error.span.start.line)
                assertEquals(1, r.error.span.start.column)
            }
        }
    }

    @Test
    fun yamlInvalidoRetornaFailure() {
        val tmp: File = createTempFile(suffix = ".yml").toFile()
        tmp.writeText(
            """
            identifiers:
              style: [ not, valid, here
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> fail("Debió fallar por YAML inválido")
            is Failure -> {
                assertTrue(
                    r.error.message.contains("Formato inválido") ||
                        r.error.message.contains("Error al leer"),
                )
                // span default
                assertEquals(1, r.error.span.start.line)
                assertEquals(1, r.error.span.start.column)
            }
        }

        tmp.delete()
    }
}
