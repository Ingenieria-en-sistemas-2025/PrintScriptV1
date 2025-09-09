package org.printscript.analyzer

class RegexNameConvention(override val id: String, pattern: Regex) : NameConvention {
    private val regex = pattern
    override fun matches(name: String): Boolean = regex.matches(name)
}
