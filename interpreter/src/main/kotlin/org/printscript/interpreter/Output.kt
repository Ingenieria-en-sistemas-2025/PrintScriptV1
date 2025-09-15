package org.printscript.interpreter

interface Output {
    fun append(s: String): Output
    fun asList(): List<String>

    companion object {
        fun empty(): Output = Collecting(emptyList())
        fun sink(): Output = object : Output {
            override fun append(s: String): Output = this
            override fun asList(): List<String> = emptyList()
            override fun toString() = "Output.Sink"
        }
    }
}
