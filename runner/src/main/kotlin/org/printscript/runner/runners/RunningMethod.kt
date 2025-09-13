package org.printscript.runner.runners

import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError

interface RunningMethod<Out> {
    fun run(version: Version, io: ProgramIo): Result<Out, RunnerError>
}
