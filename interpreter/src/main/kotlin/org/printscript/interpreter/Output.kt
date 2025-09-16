package org.printscript.interpreter

class Output private constructor(
    private val lines: MutableList<String>,
) {
    companion object { fun empty() = Output(mutableListOf()) }

    fun append(line: String): Output {
        lines.add(line) // O(1) amortizado
        return this // devolvés el mismo buffer
    }
    fun asList(): List<String> = lines.toList() // copia final
    override fun toString(): String = lines.joinToString("\n")
}
