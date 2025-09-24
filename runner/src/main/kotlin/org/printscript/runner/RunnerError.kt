package org.printscript.runner

data class RunnerError(
    val stage: Stage,
    val message: String,
    val cause: Throwable? = null,
)
