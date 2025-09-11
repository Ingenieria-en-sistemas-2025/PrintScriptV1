package org.printscript.interpreter

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.common.Type

data class Env private constructor(
    private val bindings: Map<String, Binding>, // para no romper reglas
) {
    companion object { fun empty() = Env(emptyMap()) }

    fun lookup(name: String): Binding? = bindings[name] // si var existe devuelvo su binding, sino null

    // prohibe redeclarar
    fun declare(name: String, type: Type, value: Value?, span: Span): Result<Env, InterpreterError> {
        if (name in bindings) return Failure(Redeclaration(span, name))
        val v = value ?: defaultFor(type) // si no tiene valor, le asigno
        if (!typeMatches(type, v)) {
            return Failure(IncompatibleType(span, type, runtimeName(v)))
        }
        return Success(Env(bindings + (name to Binding(type, v))))
    }

    // var ya debe estar declarada y deben coincidir los tipos
    fun assign(name: String, value: Value, span: Span): Result<Env, InterpreterError> {
        val old = bindings[name] ?: return Failure(UndeclaredVariable(span, name))
        if (!typeMatches(old.type, value)) {
            return Failure(IncompatibleType(span, old.type, runtimeName(value)))
        }
        return Success(Env(bindings + (name to old.copy(value = value)))) // actualizo el binding
    }

    private fun typeMatches(expected: Type, v: Value): Boolean =
        (expected == Type.NUMBER && v is Value.Num) || (expected == Type.STRING && v is Value.Str)

    // devuelve "number" o "string"
    private fun runtimeName(v: Value): String =
        when (v) { is Value.Num -> "number"
            is Value.Str -> "string"
            is Value.Bool -> "boolean" }

    private fun defaultFor(type: Type): Value =
        if (type == Type.NUMBER) Value.Num(0.0) else Value.Str("")
}
