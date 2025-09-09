package org.printscript.token.dsl

import org.printscript.common.Type

fun TokenBuilder.ty() = TypeDsl(this)
class TypeDsl(private val b: TokenBuilder) {
    fun numberType() = b.type(Type.NUMBER)
    fun stringType() = b.type(Type.STRING)
    fun booleanType() = b.type(Type.BOOLEAN)
}
