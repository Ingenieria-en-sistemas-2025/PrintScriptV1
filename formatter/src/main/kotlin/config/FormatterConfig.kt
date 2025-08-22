package config

data class FormatterConfig(
    val spaceBeforeColonInDecl: Boolean = false,
    val spaceAfterColonInDecl:  Boolean = true,
    val spaceAroundAssignment:  Boolean = true,
    val blankLinesBeforePrintln: Int    = 0
)