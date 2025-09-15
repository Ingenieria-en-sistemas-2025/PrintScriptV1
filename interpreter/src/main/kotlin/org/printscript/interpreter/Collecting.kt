package org.printscript.interpreter

data class Collecting(private val lines: List<String>) : Output {
    override fun append(s: String): Output = Collecting(lines + s)
    override fun asList(): List<String> = lines
}
