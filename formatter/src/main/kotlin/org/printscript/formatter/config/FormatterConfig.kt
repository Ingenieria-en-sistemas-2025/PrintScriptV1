package org.printscript.formatter.config

data class FormatterConfig(
    override val spaceBeforeColonInDecl: Boolean = false,
    override val spaceAfterColonInDecl: Boolean = true, // ← Cambié de false a true
    override val spaceAroundAssignment: Boolean = true,
    override val blankLinesAfterPrintln: Int = 1,
    override val indentSpaces: Int = 2,
    override val mandatorySingleSpaceSeparation: Boolean = false,
    override val ifBraceBelowLine: Boolean = false,
    override val ifBraceSameLine: Boolean = true,
) : FormatterOptions
