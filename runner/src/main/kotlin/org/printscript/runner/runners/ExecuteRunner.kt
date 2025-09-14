package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.Interpreting
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.tokenStream

class ExecuteRunner : RunningMethod<List<String>> {
    override fun run(version: Version, io: ProgramIo): Result<List<String>, RunnerError> {
        val w = LanguageWiringFactory.forVersion(version)
        // lexer y parserr
        val ts = tokenStream(io, w)
        val stmts = w.statementStreamFromTokens(ts)

        // int
        val interpreter = w.interpreterFor(io.inputProviderOverride)

        return when (val rr = interpreter.run(stmts)) {
            is Success -> Success(rr.value.outputs)
            is Failure -> Failure(RunnerError(Interpreting, "runtime error", rr.error))
        }
    }
}
