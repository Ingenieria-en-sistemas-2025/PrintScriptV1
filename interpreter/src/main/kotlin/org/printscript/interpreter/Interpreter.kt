package org.printscript.interpreter

import org.printscript.ast.StatementStream
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

interface Interpreter {
    fun run(stream: StatementStream): Result<RunResult, InterpreterError>
}
