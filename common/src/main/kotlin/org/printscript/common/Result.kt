package org.printscript.common
sealed interface Result<out T, out E> { // out “solo puedo producir (devolver) valores de este tipo, nunca consumirlos”.
    val isSuccess: Boolean
    val isFailure: Boolean get() = !isSuccess

    fun getOrNull(): T?
    fun errorOrNull(): E?

    // Si es org.printscript.common.Success, transforma T en U, si es org.printscript.common.Failure, lo deja igual
    fun <U> map(transform: (T) -> U): Result<U, E>

    // Diferencia con map: diferencia con map: no devuelve un valor simple (U), sino otro org.printscript.common.Result<U, E>
    // Permite encadenar operaciones que tambien pueden fallar
    fun <U> flatMap(transform: (T) -> Result<U, @UnsafeVariance E>): Result<U, E>

    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (E) -> R,
    ): R
}

data class Failure<E> (val error: E) : Result<Nothing, E> {

    override val isSuccess: Boolean = false
    override fun getOrNull(): Nothing? = null
    override fun errorOrNull(): E = error

    override fun <U> map(transform: (Nothing) -> U): Result<U, E> = this
    override fun <U> flatMap(transform: (Nothing) -> Result<U, E>): Result<U, E> = this

    override fun <R> fold(
        onSuccess: (Nothing) -> R,
        onFailure: (E) -> R,
    ): R = onFailure(error)
}

data class Success<T>(val value: T) : Result<T, Nothing> {
    override val isSuccess: Boolean = true

    override fun getOrNull(): T = value

    override fun errorOrNull(): Nothing? = null

    override fun <U> map(transform: (T) -> U): Result<U, Nothing> = Success(transform(value))

    override fun <U> flatMap(transform: (T) -> Result<U, Nothing>): Result<U, Nothing> = transform(value)

    override fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Nothing) -> R,
    ): R = onSuccess(value)
}
