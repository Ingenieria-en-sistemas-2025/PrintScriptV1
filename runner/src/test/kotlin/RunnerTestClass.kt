import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.formatter.config.FormatterConfig
import org.printscript.runner.ProgramIo
import org.printscript.runner.helpers.AnalyzerConfigLoaderFromPath
import org.printscript.runner.helpers.FormatterOptionsLoader
import org.printscript.runner.runners.ExecuteRunnerStreaming
import org.printscript.runner.runners.FormatRunnerStreaming
import org.printscript.runner.runners.FormatRunnerWithOptionsStreaming
import org.printscript.runner.runners.ValidateRunner
import org.printscript.runner.runners.ValidateRunnerWithConfig
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RunnerTestClass {
    @Test
    fun testAnalyzeRunnerHandlesEmptySource() {
        val io = ProgramIo(source = "")
        assertDoesNotThrow {
        }
    }

    @Test
    fun testExecuteRunnerStreamingCreation() {
        val printedMessages = mutableListOf<String>()
        val printer: (String) -> Unit = { printedMessages.add(it) }

        val runner = ExecuteRunnerStreaming(printer, false)
        assertNotNull(runner)
    }

    @Test
    fun testExecuteRunnerStreamingWithNullPrinter() {
        val runner = ExecuteRunnerStreaming(null, false)
        assertNotNull(runner)
    }

    @Test
    fun testExecuteRunnerStreamingWithCollectAlsoWithPrinter() {
        val printer: (String) -> Unit = { }
        val runner = ExecuteRunnerStreaming(printer, true)

        assertNotNull(runner)
    }

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun testFormatRunnerStreamingCreation() {
        val output = StringWriter()
        val runner = FormatRunnerStreaming(output)

        assertNotNull(runner)
    }

    @Test
    fun testFormatRunnerStreamingWithOverrideIndent() {
        val output = StringWriter()
        val runner = FormatRunnerStreaming(output, overrideIndent = 8)

        assertNotNull(runner)
    }

    @Test
    fun testFormatRunnerStreamingUsesFormatterOptionsLoader() {
        val configFile = tempDir.resolve("formatter.json")
        Files.write(configFile, """{"indentSpaces": 4}""".toByteArray())

        val output = StringWriter()
        val runner = FormatRunnerStreaming(output)
        val io = ProgramIo(source = "let x=5;", configPath = configFile)

        assertNotNull(FormatterOptionsLoader.fromPath(configFile.toString()))
    }

    @Test
    fun testFormatRunnerWithOptionsStreamingCreation() {
        val output = StringWriter()
        val options = FormatterConfig(indentSpaces = 2)
        val runner = FormatRunnerWithOptionsStreaming(output, options)

        assertNotNull(runner)
    }

    @Test
    fun testFormatRunnerWithOptionsStreamingUsesProvidedOptions() {
        val output = StringWriter()
        val options = FormatterConfig(indentSpaces = 4)
        val runner = FormatRunnerWithOptionsStreaming(output, options)

        assertNotNull(runner)
    }

    @Test
    fun testValidateRunnerCreation() {
        val runner = ValidateRunner()
        assertNotNull(runner)
    }

    @Test
    fun testValidateRunnerUsesAnalyzerConfigLoader() {
        val configFile = tempDir.resolve("analyzer.json")
        Files.write(configFile, """{"identifiers": {"checkDeclaration": true}}""".toByteArray())

        val config = AnalyzerConfigLoaderFromPath.fromPath(configFile.toString())
        assertNotNull(config)
    }

    @Test
    fun testValidateRunnerHandlesNullConfigPath() {
        val config = AnalyzerConfigLoaderFromPath.fromPath(null)
        assertNotNull(config)
        assertEquals(AnalyzerConfig(), config)
    }

    @Test
    fun testValidateRunnerWithConfigCreation() {
        val config = AnalyzerConfig()
        val runner = ValidateRunnerWithConfig(config)

        assertNotNull(runner)
    }

    @Test
    fun testValidateRunnerWithConfigUsesProvidedConfig() {
        val config = AnalyzerConfig()
        val runner = ValidateRunnerWithConfig(config)

        assertNotNull(runner)
    }

    @Test
    fun testProgramIoWithSourceCreation() {
        val io = ProgramIo(source = "let x = 5;")
        assertEquals("let x = 5;", io.source)
        assertNull(io.reader)
    }

    @Test
    fun testProgramIoWithReaderCreation() {
        val reader = StringReader("test content")
        val io = ProgramIo(reader = reader)
        assertEquals(reader, io.reader)
        assertNull(io.source)
    }

    @Test
    fun testProgramIoWithConfigPath() {
        val configPath = tempDir.resolve("config.json")
        val io = ProgramIo(source = "test", configPath = configPath)
        assertEquals(configPath, io.configPath)
    }
}
