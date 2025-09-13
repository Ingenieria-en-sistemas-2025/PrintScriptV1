package org.printscript.interpreter

import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

sealed interface Progress {
    data class Continue(val state: ProgramState) : Progress
    data class Done(val result: Result<RunResult, InterpreterError>) : Progress
}
