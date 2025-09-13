package org.printscript.interpreter

import org.printscript.ast.StatementStream

// estado de la historia en un pto del tiempo
data class ProgramState(
    val remainingStream: StatementStream,
    val currentEnv: Env,
    val currentOut: Output,
)
