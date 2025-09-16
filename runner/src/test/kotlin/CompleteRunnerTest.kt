import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.formatter.config.FormatterConfig
import org.printscript.runner.Lexing
import org.printscript.runner.Parsing
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerDiagnosticCollector
import org.printscript.runner.RunnerError
import org.printscript.runner.ValidationReport
import org.printscript.runner.hasErrors
import org.printscript.runner.helpers.AnalyzerConfigLoaderFromPath
import org.printscript.runner.helpers.FormatterOptionsLoader
import org.printscript.runner.onlyWarnings
import org.printscript.runner.runners.ExecuteRunnerStreaming
import org.printscript.runner.runners.FormatRunnerStreaming
import org.printscript.runner.runners.FormatRunnerWithOptionsStreaming
import org.printscript.runner.runners.RunningMethod
import org.printscript.runner.runners.ValidateRunner
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CompleteRunnerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testRunnerErrorHoldsStageMessageAndCause() {
        val cause = RuntimeException("Test exception")
        val error = RunnerError(Parsing, "Parse error occurred", cause)

        assertEquals(Parsing, error.stage)
        assertEquals("Parse error occurred", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun testRunnerErrorCanHaveNullCause() {
        val error = RunnerError(Lexing, "Lexing failed")

        assertEquals(Lexing, error.stage)
        assertEquals("Lexing failed", error.message)
        assertNull(error.cause)
    }

    @Test
    fun testRunnerDiagnosticCollectorStartsEmpty() {
        val collector = RunnerDiagnosticCollector()
        assertTrue(collector.diagnostics.isEmpty())
    }

    @Test
    fun testProgramIoRequiresEitherSourceOrReader() {
        val programIo1 = ProgramIo(source = "test source")
        assertEquals("test source", programIo1.source)
        assertNull(programIo1.reader)

        val reader = StringReader("test")
        val programIo2 = ProgramIo(reader = reader)
        assertNull(programIo2.source)
        assertEquals(reader, programIo2.reader)
    }

    @Test
    fun testExecuteRunnerStreamingCreation() {
        val printedMessages = mutableListOf<String>()
        val printer: (String) -> Unit = { printedMessages.add(it) }

        val runner = ExecuteRunnerStreaming(printer, false)
        assertNotNull(runner)
        assertTrue(runner is RunningMethod<Unit>)
    }

    @Test
    fun testExecuteRunnerStreamingWithNullPrinter() {
        val runner = ExecuteRunnerStreaming(null, false)
        assertNotNull(runner)
        assertTrue(runner is RunningMethod<Unit>)
    }

    @Test
    fun testExecuteRunnerStreamingWithCollectAlsoWithPrinter() {
        val printer: (String) -> Unit = { }
        val runner = ExecuteRunnerStreaming(printer, true)

        assertNotNull(runner)
        assertTrue(runner is RunningMethod<Unit>)
    }

    @Test
    fun testFormatRunnerStreamingCreation() {
        val output = StringWriter()
        val runner = FormatRunnerStreaming(output)

        assertNotNull(runner)
        assertTrue(runner is RunningMethod<Unit>)
    }

    @Test
    fun testFormatRunnerStreamingWithOverrideIndent() {
        val output = StringWriter()
        val runner = FormatRunnerStreaming(output, overrideIndent = 8)

        assertNotNull(runner)
        assertTrue(runner is RunningMethod<Unit>)
    }

    @Test
    fun testFormatRunnerWithOptionsStreamingCreation() {
        val output = StringWriter()
        val options = FormatterConfig(indentSpaces = 2)
        val runner = FormatRunnerWithOptionsStreaming(output, options)

        assertNotNull(runner)
        assertTrue(runner is RunningMethod<Unit>)
    }

    @Test
    fun testValidateRunnerCreation() {
        val runner = ValidateRunner()
        assertNotNull(runner)
        assertTrue(runner is RunningMethod<ValidationReport>)
    }

    @Test
    fun testAnalyzerConfigLoaderFromPathHandlesNullPath() {
        val config = AnalyzerConfigLoaderFromPath.fromPath(null)
        assertNotNull(config)
        assertEquals(AnalyzerConfig(), config)
    }

    @Test
    fun testAnalyzerConfigLoaderFromPathHandlesBlankPath() {
        val config = AnalyzerConfigLoaderFromPath.fromPath("   ")
        assertNotNull(config)
        assertEquals(AnalyzerConfig(), config)
    }

    @Test
    fun testAnalyzerConfigLoaderFromPathHandlesNonExistentFile() {
        val nonExistentPath = tempDir.resolve("nonexistent.json").toString()
        val config = AnalyzerConfigLoaderFromPath.fromPath(nonExistentPath)
        assertNotNull(config)
        assertEquals(AnalyzerConfig(), config)
    }

    @Test
    fun testAnalyzerConfigLoaderFromPathLoadsValidConfig() {
        val configFile = tempDir.resolve("config.json")
        Files.write(configFile, """{"identifiers": {"checkDeclaration": true}}""".toByteArray())

        val config = AnalyzerConfigLoaderFromPath.fromPath(configFile.toString())
        assertNotNull(config)
    }

    @Test
    fun testFormatterOptionsLoaderFromPathHandlesNullPath() {
        val options = FormatterOptionsLoader.fromPath(null)
        assertNotNull(options)
        assertEquals(FormatterConfig(), options)
    }

    @Test
    fun testFormatterOptionsLoaderFromPathHandlesBlankPath() {
        val options = FormatterOptionsLoader.fromPath("   ")
        assertNotNull(options)
        assertEquals(FormatterConfig(), options)
    }

    @Test
    fun testFormatterOptionsLoaderFromPathHandlesNonExistentFile() {
        val nonExistentPath = tempDir.resolve("nonexistent.json").toString()
        val options = FormatterOptionsLoader.fromPath(nonExistentPath)
        assertNotNull(options)
        assertEquals(FormatterConfig(), options)
    }

    @Test
    fun testFormatterOptionsLoaderFromPathLoadsValidConfig() {
        val configFile = tempDir.resolve("formatter.json")
        Files.write(configFile, """{"indentSpaces": 4}""".toByteArray())

        val options = FormatterOptionsLoader.fromPath(configFile.toString())
        assertNotNull(options)
    }

    @Test
    fun testEmptyDiagnosticList() {
        val emptyList = emptyList<Diagnostic>()
        assertFalse(emptyList.hasErrors())
        assertTrue(emptyList.onlyWarnings().isEmpty())
    }
}
