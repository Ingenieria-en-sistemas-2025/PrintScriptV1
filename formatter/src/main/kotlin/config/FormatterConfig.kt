package config

data class FormatterConfig(
    override val spaceBeforeColonInDecl: Boolean = false,
    override val spaceAfterColonInDecl: Boolean = true,
    override val spaceAroundAssignment: Boolean = true,
    override val blankLinesBeforePrintln: Int = 0,
) : FormatterOptions
