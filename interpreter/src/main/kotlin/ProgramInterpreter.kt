import org.example.InterpreterError
import org.example.Output

class ProgramInterpreter(
    private val executor: StmtExecutor,
    private val initialEnv: Env = Env.empty(),
    private val initialOut: Output = Output.empty()) : Interpreter {

    override fun run(program: ProgramNode): Result<RunResult, InterpreterError> {
        var env = initialEnv
        var out = initialOut

        for (stmt in program.statements){
            when (val step = executor.execute(stmt, env, out)){
                is Success -> { env = step.value.env; out = step.value.out }

                is Failure -> return Failure(step.error)
            }
        }
        return Success(RunResult(outputs = out.asList(), finalEnv = env))
    }

}