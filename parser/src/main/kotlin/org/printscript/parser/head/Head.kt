package org.printscript.parser.head

import org.printscript.common.Keyword

sealed interface Head
data object Assign : Head
data class Kw(val kw: Keyword) : Head
data object Unknown : Head
