package org.printscript.interpreter
interface InputProvider { fun read(prompt: String): String }

object StdInProvider : InputProvider {
    override fun read(prompt: String): String =
        readlnOrNull().orEmpty() // NO imprime el prompt (el TCK no captura stdout)
}

object NoInputProvider : InputProvider {
    override fun read(prompt: String): String =
        error("Input no disponible en esta versión")
}
