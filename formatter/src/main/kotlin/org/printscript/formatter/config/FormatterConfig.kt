package org.printscript.formatter.config

data class FormatterConfig(
    override val spaceBeforeColonInDecl: Boolean? = null,
    override val spaceAfterColonInDecl: Boolean? = null,
    override val spaceAroundAssignment: Boolean? = null,
    override val blankLinesAfterPrintln: Int? = null,
    override val indentSpaces: Int = 2,
    override val mandatorySingleSpaceSeparation: Boolean? = null,
    override val ifBraceBelowLine: Boolean? = null,
    override val ifBraceSameLine: Boolean? = null,
) : FormatterOptions
