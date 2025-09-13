package org.printscript.analyzer.rules

import org.printscript.analyzer.config.IdentifiersConfig

object IdentifierNaming {
    fun from(config: IdentifiersConfig): NameConvention =
        when (config.style) {
            IdentifierStyle.CAMEL_CASE -> BuiltinConventions.camel
            IdentifierStyle.SNAKE_CASE -> BuiltinConventions.snake
        }
}
