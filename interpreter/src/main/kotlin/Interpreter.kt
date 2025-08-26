interface Interpreter {
    fun run(program: ProgramNode): Result<RunResult, InterpreterError>
}
