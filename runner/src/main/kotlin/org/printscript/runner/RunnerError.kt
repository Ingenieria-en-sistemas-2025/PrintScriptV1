package org.printscript.runner

data class RunnerError(
    val stage: Stage,
    val message: String,
    val cause: Any? = null, // aca seria generalmente el labelerror
)
