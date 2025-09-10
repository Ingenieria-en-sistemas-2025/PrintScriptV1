package org.printscript.analyzer

object BuiltinConventions {
    val camel = RegexNameConvention("CAMEL_CASE", Regex("[a-z]+([A-Z][a-z0-9]*)*"))
    val snake = RegexNameConvention("SNAKE_CASE", Regex("[a-z]+(_[a-z0-9]+)*"))
}
