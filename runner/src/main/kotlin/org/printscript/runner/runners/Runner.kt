package org.printscript.runner.runners

import org.printscript.analyzer.Diagnostic
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterOptions
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerDiagnosticCollector
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.ValidationReport
import org.printscript.runner.helpers.AnalyzerConfigLoaderFromStream
import org.printscript.runner.tokenStream

object Runner {
    fun validate(v: Version, io: ProgramIo): Result<ValidationReport, RunnerError> =
        ValidateRunner().run(v, io)

    fun analyze(v: Version, io: ProgramIo): Result<List<Diagnostic>, RunnerError> =
        AnalyzeRunner.run(v, io)

    fun format(v: Version, io: ProgramIo, options: FormatterOptions? = null, overrideIndent: Int? = null): Result<String, RunnerError> {
        val out = StringBuilder()
        val method = if (options != null) {
            FormatRunnerWithOptionsStreaming(out, options)
        } else {
            FormatRunnerStreaming(out, overrideIndent)
        }
        return method.run(v, io).map { out.toString() }
    }

    fun execute(v: Version, io: ProgramIo, printer: ((String) -> Unit)? = null, collect: Boolean = false): Result<Unit, RunnerError> {
        val method = ExecuteRunnerStreaming(printer, collect)
        return method.run(v, io)
    }

    fun analyzeWithConfigStream(
        v: Version,
        io: ProgramIo,
        config: java.io.InputStream?,
        onConfigError: ((String) -> Unit)? = null,
    ): Result<List<Diagnostic>, RunnerError> {
        val w = LanguageWiringFactory.forVersion(v)

        val ts = runCatching { tokenStream(io, w) }
            .getOrElse { t -> return Failure(RunnerError(Stage.Lexing, "lexing failed", t)) }

        val stmts = runCatching { w.statementStreamFromTokens(ts) }
            .getOrElse { t -> return Failure(RunnerError(Stage.Parsing, "parsing failed", t)) }

        val cfg = AnalyzerConfigLoaderFromStream.fromStream(config) { msg -> onConfigError?.invoke(msg) }

        val emitter = RunnerDiagnosticCollector()
        return when (val ar = w.analyzer.analyze(stmts, cfg, emitter)) {
            is Success -> Success(emitter.diagnostics)
            is Failure -> Failure(RunnerError(Stage.Analyzing, "analyze error", ar.error as? Throwable))
        }
    }

    fun parse(v: Version, io: ProgramIo): Result<List<Diagnostic>, RunnerError> =
        ParseRunner.run(v, io)
}

data class ExecutionReport(val lines: List<String>)
