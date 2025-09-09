package org.printscript.interpreter

data class Output(private val lines: List<String>) {
    companion object { fun empty() = Output(emptyList()) }

    fun append(line: String): Output = Output(lines + line)
    fun asList(): List<String> = lines
    override fun toString(): String = lines.joinToString("\n")
}
