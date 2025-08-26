
sealed interface Result<out T, out E> { // out “solo puedo producir (devolver) valores de este tipo, nunca consumirlos”.
    val isSuccess: Boolean
    val isFailure: Boolean get() = !isSuccess

    fun getOrNull(): T?
    fun errorOrNull(): E?

    // Si es Success, transforma T en U, si es Failure, lo deja igual
    fun <U> map(transform: (T) -> U): Result<U, E>

    // Diferencia con map: diferencia con map: no devuelve un valor simple (U), sino otro Result<U, E>
    // Permite encadenar operaciones que tambien pueden fallar
    fun <U> flatMap(transform: (T) -> Result<U, @UnsafeVariance E>): Result<U, E>

    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (E) -> R,
    ): R
}
