import org.example.InterpreterError

interface Interpreter {
    fun run(program: ProgramNode): Result<RunResult, InterpreterError>
}