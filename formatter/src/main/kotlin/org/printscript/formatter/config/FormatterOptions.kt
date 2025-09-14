package org.printscript.formatter.config

interface FormatterOptions {
    val spaceBeforeColonInDecl: Boolean
    val spaceAfterColonInDecl: Boolean
    val spaceAroundAssignment: Boolean
    val blankLinesAfterPrintln: Int
    val indentSpaces: Int
    val mandatorySingleSpaceSeparation: Boolean
    val ifBraceBelowLine: Boolean
    val ifBraceSameLine: Boolean
}
