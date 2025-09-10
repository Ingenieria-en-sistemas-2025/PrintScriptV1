package org.printscript.formatter.factories

import org.printscript.formatter.Formatter
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions

object GlobalFormatterFactory {
    fun forVersion(
        version: String,
        options: FormatterOptions = FormatterConfig(),
    ): Formatter? = when (version) {
        "1.0" -> FormatterFactoryV10.create(options)
        "1.1" -> FormatterFactoryV11.create(options)
        else -> null
    }
}
