package org.printscript.interpreter

import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.interpreter.errors.InterpreterError

class ProgramInterpreter(
    private val executor: StmtExecutor,
    private val initialEnv: Env = Env.empty(),
    private val initialOut: Output = Output.empty(),
) : Interpreter {

    override fun run(stream: StatementStream): Result<RunResult, InterpreterError> {
        val initialProgress = Progress.Continue(
            ProgramState(stream, initialEnv, initialOut),
        ) // est inicial empiezo a leer el stream

        fun stepOnce(progress: Progress.Continue): Progress {
            val state = progress.state
            val step = state.remainingStream.nextStep()

            return when (step) {
                is Step.Item -> {
                    val executionResult = executor.execute(
                        step.statement,
                        state.currentEnv,
                        state.currentOut,
                    )
                    when (executionResult) {
                        is Success -> {
                            val nextState = ProgramState(
                                remainingStream = step.next,
                                currentEnv = executionResult.value.env,
                                currentOut = executionResult.value.out,
                            )
                            Progress.Continue(nextState)
                        }
                        is Failure -> {
                            Progress.Done(Failure(executionResult.error))
                        }
                    }
                }
                is Step.Error -> {
                    Progress.Done(Failure(step.error))
                }
                is Step.Eof -> {
                    Progress.Done(
                        Success(
                            RunResult(
                                outputs = state.currentOut.asList(),
                                finalEnv = state.currentEnv,
                            ),
                        ),
                    )
                }
            }
        }

        val finalProgress: Progress.Done =
            generateSequence<Progress>(initialProgress) { current ->
                when (current) {
                    is Progress.Continue -> stepOnce(current)
                    is Progress.Done -> null
                }
            }.last() as Progress.Done

        return finalProgress.result
    }
}
