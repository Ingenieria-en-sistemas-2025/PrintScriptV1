package org.printscript.common

interface LabeledError {
    val span: Span
    val message: String
    fun humanReadable(): String = "$message @ ${span.start.line}:${span.start.column}"

    companion object {
        fun of(span: Span, message: String): LabeledError =
            object : LabeledError {
                override val span: Span = span
                override val message: String = message
            }
    }
}
