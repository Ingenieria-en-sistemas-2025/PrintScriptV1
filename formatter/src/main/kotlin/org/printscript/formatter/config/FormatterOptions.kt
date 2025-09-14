package org.printscript.formatter.config

interface FormatterOptions {
    val spaceBeforeColonInDecl: Boolean
    val spaceAfterColonInDecl: Boolean
    val spaceAroundAssignment: Boolean
    val blankLinesBeforePrintln: Int
    val indentSpaces: Int
    val mandatorySingleSpaceSeparation: Boolean
}
