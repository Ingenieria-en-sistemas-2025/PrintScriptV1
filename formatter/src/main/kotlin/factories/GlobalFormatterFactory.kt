package factories

import Formatter
import config.FormatterConfig
import config.FormatterOptions

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
