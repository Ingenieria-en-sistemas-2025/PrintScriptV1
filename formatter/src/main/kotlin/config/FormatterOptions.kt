package config

interface FormatterOptions {
    val spaceBeforeColonInDecl: Boolean
    val spaceAfterColonInDecl: Boolean
    val spaceAroundAssignment: Boolean
    val blankLinesBeforePrintln: Int
}
