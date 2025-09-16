import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.loader.AnalyzerConfigLoader
import org.printscript.analyzer.loader.ConfigFormat
import org.printscript.analyzer.loader.JsonConfigReader
import org.printscript.analyzer.rules.IdentifierStyle
import org.printscript.common.Failure
import org.printscript.common.Success
import java.io.File
import java.io.InputStream
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AnalyzerConfigLoaderMoreTests {
    // InputStream que fuerza IOException al leer (para cubrir branches de IO en readers)
    private class ThrowingInputStream : InputStream() {
        override fun read(): Int = throw java.io.IOException("boom io")
    }

    @Test
    fun overrideFormatoYamlSobreArchivoJson_extension_no_manda() {
        // Archivo .json pero con contenido YAML y forzamos format=YAML
        val tmp: File = createTempFile(suffix = ".json").toFile()
        tmp.writeText(
            """
            identifiers:
              style: SNAKE_CASE
            printlnRule:
              enabled: false
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp, format = ConfigFormat.YAML)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.SNAKE_CASE, cfg.identifiers.style)
                assertFalse(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No debía fallar: ${r.error.message}")
        }
        tmp.delete()
    }

    @Test
    fun extensionDesconocida_caeEnJsonPorDefault() {
        val tmp: File = createTempFile(suffix = ".conf").toFile()
        tmp.writeText(
            """
            {
              "identifiers": { "style": "CAMEL_CASE", "checkReferences": true, "failOnViolation": false },
              "printlnRule": { "enabled": true }
            }
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.CAMEL_CASE, cfg.identifiers.style)
                assertTrue(cfg.identifiers.checkReferences)
                assertFalse(cfg.identifiers.failOnViolation)
                assertTrue(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No debía fallar: ${r.error.message}")
        }
        tmp.delete()
    }

    @Test
    fun lecturaYaml_ok_conExtensionYaml() {
        val tmp: File = createTempFile(suffix = ".yaml").toFile()
        tmp.writeText(
            """
            identifiers:
              style: CAMEL_CASE
              checkReferences: false
            printlnRule:
              enabled: true
            """.trimIndent(),
        )

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.CAMEL_CASE, cfg.identifiers.style)
                assertFalse(cfg.identifiers.checkReferences)
                assertTrue(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No debía fallar: ${r.error.message}")
        }
        tmp.delete()
    }

    @Test
    fun jsonInvalidoRetornaFailure_formatoInvalido() {
        val tmp: File = createTempFile(suffix = ".json").toFile()
        tmp.writeText("""{ "identifiers": { "style": "NOT_A_STYLE",  } }""") // coma colgante + enum inválido

        when (val r = AnalyzerConfigLoader.fromFile(tmp)) {
            is Success -> fail("Debió fallar por JSON inválido")
            is Failure -> {
                // Aceptamos cualquiera de los dos mensajes según el punto exacto del parser
                assertTrue(
                    r.error.message.contains("Formato inválido") ||
                        r.error.message.contains("Error de IO leyendo JSON") ||
                        r.error.message.contains("Config JSON inválida"),
                )
                assertTrue(r.error.span.start.line >= 1)
                assertTrue(r.error.span.start.column >= 1)
            }
        }
        tmp.delete()
    }

    @Test
    fun jsonReader_errorDeIO() {
        val reader = JsonConfigReader()
        val r = reader.load(ThrowingInputStream())
        when (r) {
            is Success -> fail("Debía fallar por IO")
            is Failure -> {
                assertTrue(r.error.message.startsWith("Error de IO leyendo JSON:"))
                assertEquals(1, r.error.span.start.line)
                assertEquals(1, r.error.span.start.column)
            }
        }
    }
}
