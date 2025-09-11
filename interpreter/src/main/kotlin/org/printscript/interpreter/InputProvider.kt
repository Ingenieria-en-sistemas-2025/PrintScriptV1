package org.printscript.interpreter

interface InputProvider { fun read(prompt: String): String }
object StdInProvider : InputProvider {
    override fun read(prompt: String) = kotlin.run {
        print(prompt)
        readLine().orEmpty()
    }
}
object NoInputProvider : InputProvider {
    override fun read(prompt: String): String =
        error("Input no disponible en esta versión")
}
