
import org.printscript.analyzer.loader.AnalyzerConfigLoader
import org.printscript.analyzer.rules.IdentifierStyle
import org.printscript.common.Failure
import org.printscript.common.Success
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class AnalyzerConfigLoaderResourcesTest {

    @Test
    fun yamlFixtureOk() {
        val file = resourceFile("/analyzer/valid.yml")
        when (val r = AnalyzerConfigLoader.fromFile(file)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.SNAKE_CASE, cfg.identifiers.style)
                assertTrue(cfg.identifiers.checkReferences)
                assertTrue(cfg.identifiers.failOnViolation)
                assertFalse(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No pudo leer YAML: ${r.error.message}")
        }
    }

    @Test
    fun jsonFixtureOk() {
        val file = resourceFile("/analyzer/valid.json")
        when (val r = AnalyzerConfigLoader.fromFile(file)) {
            is Success -> {
                val cfg = r.value
                assertEquals(IdentifierStyle.CAMEL_CASE, cfg.identifiers.style)
                assertFalse(cfg.identifiers.checkReferences)
                assertTrue(cfg.printlnRule.enabled)
            }
            is Failure -> fail("No pudo leer JSON: ${r.error.message}")
        }
    }

    @Test
    fun yamlInvalidoFalla() {
        val file = resourceFile("/analyzer/invalid.yml")
        when (val r = AnalyzerConfigLoader.fromFile(file)) {
            is Success -> fail("Debió fallar por YAML inválido")
            is Failure -> {
                val msg = r.error.message.lowercase()
                assertTrue(
                    msg.contains("formato inválido") || msg.contains("error al leer"),
                    "Mensaje inesperado: ${r.error.message}",
                )

                assertTrue(r.error.span.start.line >= 1, "line debe ser >= 1, fue ${r.error.span.start.line}")
                assertTrue(r.error.span.start.column >= 1, "column debe ser >= 1, fue ${r.error.span.start.column}")
            }
        }
    }

    /** Devuelve un File de un recurso copiado por Gradle (test resources). */
    private fun resourceFile(path: String): File {
        val url = requireNotNull(this::class.java.getResource(path)) { "No existe $path" }
        return File(url.toURI())
    }
}
