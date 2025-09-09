package org.printscript.token

import org.printscript.common.Span
import org.printscript.common.Type

data class TypeToken(val type: Type, override val span: Span) : WordLikeToken {
    override fun toString() = "TYPE($type)"
}
