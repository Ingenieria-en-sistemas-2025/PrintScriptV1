package org.printscript.ast

interface StatementStream {
    fun nextStep(): Step
}
