package org.printscript.formatter.config

data class FormatterConfig(
    override val spaceBeforeColonInDecl: Boolean = false,
    override val spaceAfterColonInDecl: Boolean = false,
    override val spaceAroundAssignment: Boolean = true,
    override val blankLinesBeforePrintln: Int = 0,
    override val indentSpaces: Int = 4,
    override val mandatorySingleSpaceSeparation: Boolean = false,
) : FormatterOptions
