package org.printscript.formatter.config

data class FormatterConfig(
    override val spaceBeforeColonInDecl: Boolean = false,
    override val spaceAfterColonInDecl: Boolean = true,
    override val spaceAroundAssignment: Boolean = true,
    override val blankLinesBeforePrintln: Int = 0,
    override val indentSpaces: Int = 4,
) : FormatterOptions
