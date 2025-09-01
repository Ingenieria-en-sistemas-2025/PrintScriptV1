object IdentifierNaming {
    fun from(config: IdentifiersConfig): NameConvention =
        config.customRegex?.let { RegexNameConvention("CUSTOM", Regex(it)) }
            ?: when (config.style) {
                IdentifierStyle.CAMEL_CASE -> BuiltinConventions.camel
                IdentifierStyle.SNAKE_CASE -> BuiltinConventions.snake
            }
}
