package org.printscript.interpreter

import org.printscript.ast.ProgramNode
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

interface Interpreter {
    fun run(program: ProgramNode): Result<RunResult, InterpreterError>
}
