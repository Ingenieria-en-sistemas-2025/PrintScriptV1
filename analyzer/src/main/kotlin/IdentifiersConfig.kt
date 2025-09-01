data class IdentifiersConfig(
    val style: IdentifierStyle = IdentifierStyle.CAMEL_CASE,
    val customRegex: String? = null,
    val checkReferences: Boolean = false,
    val failOnViolation: Boolean = false, // por defecto solo warning.
)
