package org.printscript.interpreter

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.common.Type
import org.printscript.interpreter.errors.ConstAssignment
import org.printscript.interpreter.errors.IncompatibleType
import org.printscript.interpreter.errors.InterpreterError
import org.printscript.interpreter.errors.Redeclaration
import org.printscript.interpreter.errors.UndeclaredVariable
import java.util.Collections.emptyMap

typealias Printer = (String) -> Unit

@ConsistentCopyVisibility
data class Env internal constructor(
    private val bindings: Map<String, Binding>, // para no romper reglas
    private val input: InputProvider,
    private val printer: Printer? = null,
) {
    companion object {
        fun empty(input: InputProvider = NoInputProvider): Env =
            Env(emptyMap(), input, null)
    }

    fun lookup(name: String): Binding? = bindings[name] // si var existe devuelvo su binding, sino null

    // prohibe redeclarar
    fun declareVar(name: String, type: Type, value: Value?, span: Span): Result<Env, InterpreterError> {
        if (name in bindings) return Failure(Redeclaration(span, name))
        val v = value ?: defaultFor(type)
        if (!typeMatches(type, v)) return Failure(IncompatibleType(span, type, runtimeName(v)))
        return Success(copy(bindings = bindings + (name to Binding(type, v, isConst = false))))
    }

    fun declareConst(name: String, type: Type, value: Value?, span: Span): Result<Env, InterpreterError> {
        if (name in bindings) return Failure(Redeclaration(span, name))
        val v = value ?: defaultFor(type)
        if (!typeMatches(type, v)) return Failure(IncompatibleType(span, type, runtimeName(v)))
        return Success(copy(bindings = bindings + (name to Binding(type, v, isConst = true))))
    }

    fun withPrinter(p: Printer?): Env = copy(printer = p)
    fun emit(line: String) { printer?.invoke(line) }
    fun hasPrinter(): Boolean = printer != null

    fun withInput(newInput: InputProvider): Env = copy(input = newInput)

    fun inputProvider(): InputProvider = input // accede al provider actual

    // var ya debe estar declarada y deben coincidir los tipos
    fun assign(name: String, value: Value, span: Span): Result<Env, InterpreterError> {
        val old = bindings[name] ?: return Failure(UndeclaredVariable(span, name))
        if (old.isConst) {
            return Failure(ConstAssignment(span, name))
        }
        if (!typeMatches(old.type, value)) {
            return Failure(IncompatibleType(span, old.type, runtimeName(value)))
        }
        return Success(copy(bindings = bindings + (name to old.copy(value = value))))
    }

    fun readEnvVar(name: String): String? = System.getenv(name)

    fun readInput(prompt: String): String {
        emit(prompt)
        return input.read(prompt)
    }

    private fun typeMatches(expected: Type, v: Value): Boolean =
        (expected == Type.NUMBER && v is Value.Num) ||
            (expected == Type.STRING && v is Value.Str) ||
            (expected == Type.BOOLEAN && v is Value.Bool)

    private fun runtimeName(v: Value): String =
        when (v) { is Value.Num -> "number"
            is Value.Str -> "string"
            is Value.Bool -> "boolean" }

    private fun defaultFor(type: Type): Value =
        when (type) {
            Type.NUMBER -> Value.Num(0.0)
            Type.STRING -> Value.Str("")
            Type.BOOLEAN -> Value.Bool(false)
        }
}
