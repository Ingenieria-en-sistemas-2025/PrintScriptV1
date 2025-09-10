package org.printscript.analyzer

interface NameConvention {
    val id: String
    fun matches(name: String): Boolean
}
