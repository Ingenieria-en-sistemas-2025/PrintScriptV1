package org.printscript.runner.runners

import org.printscript.analyzer.Diagnostic
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterOptions
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.ValidationReport

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
}

data class ExecutionReport(val lines: List<String>)
