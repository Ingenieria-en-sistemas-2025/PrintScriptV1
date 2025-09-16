package org.printscript.runner.helpers

import org.printscript.interpreter.InputProvider
import org.printscript.runner.ProgramIo
import java.io.Reader

object IoFactory {
    fun fromReader(reader: Reader): ProgramIo =
        ProgramIo(reader = reader)

    fun fromReaderWithInput(reader: Reader, input: ((String) -> String)?): ProgramIo {
        val provider: InputProvider? = input?.let { fn ->
            object : InputProvider { override fun read(prompt: String): String = fn(prompt) }
        }
        return ProgramIo(reader = reader, inputProviderOverride = provider)
    }
}
