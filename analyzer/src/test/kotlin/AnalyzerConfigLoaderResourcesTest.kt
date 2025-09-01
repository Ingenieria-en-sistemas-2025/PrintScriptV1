
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
                assertTrue(
                    r.error.message.contains("Formato inválido") ||
                        r.error.message.contains("Error al leer"),
                )
                assertEquals(1, r.error.span.start.line)
                assertEquals(1, r.error.span.start.column)
            }
        }
    }

    /** Devuelve un File de un recurso copiado por Gradle (test resources). */
    private fun resourceFile(path: String): File {
        val url = requireNotNull(this::class.java.getResource(path)) { "No existe $path" }
        return File(url.toURI())
    }
}
