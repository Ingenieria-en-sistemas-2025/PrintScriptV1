
sealed interface Result<out T, out E> { // out “solo puedo producir (devolver) valores de este tipo, nunca consumirlos”.
    val isSuccess: Boolean
    val isFailure: Boolean get() = !isSuccess

    fun getOrNull (): T?
    fun errorOrNull(): E?

    fun <U> map(transform: (T) -> U): Result<U, E>

    fun <U> flatMap(transform: (T) -> Result<U, @UnsafeVariance E>): Result<U, E>

    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (E) -> R,
    ): R

}
