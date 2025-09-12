package org.printscript.formatter.factories

import org.printscript.common.Version
import org.printscript.formatter.Formatter
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions

object GlobalFormatterFactory {
    fun forVersion(
        version: Version,
        options: FormatterOptions = FormatterConfig(),
    ): Formatter? = when (version) {
        Version.V0 -> FormatterFactoryV10.create(options)
        Version.V1 -> FormatterFactoryV11.create(options)
    }
}
