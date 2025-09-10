package org.printscript.interpreter

import org.printscript.ast.ProgramNode
import org.printscript.common.Result

interface Interpreter {
    fun run(program: ProgramNode): Result<RunResult, InterpreterError>
}
