package org.printscript.interpreter

import org.printscript.common.Version

data class InterpreterConfig(val initialEnv: Env)

object InterpreterConfigFactory {
    fun forVersion(v: Version): InterpreterConfig = InterpreterConfig(initialEnv = Env.empty(inputFor(v)))

    private fun inputFor(v: Version): InputProvider = when (v) {
        Version.V0 -> NoInputProvider
        Version.V1 -> StdInProvider
    }
}
