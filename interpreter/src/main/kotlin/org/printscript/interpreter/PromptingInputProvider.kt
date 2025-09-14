package org.printscript.interpreter

typealias Echo = (String) -> Unit

// decorador de InputProvider que hace echo del prompt, entonces el prompt queda en Output
class PromptingInputProvider(
    private val base: InputProvider,
    private val echo: (String) -> Unit,
) : InputProvider {
    override fun read(prompt: String): String {
        echo(prompt) // agrego el prompt al Output del programa
        return base.read(prompt) // delego la lectura real
    }
}
