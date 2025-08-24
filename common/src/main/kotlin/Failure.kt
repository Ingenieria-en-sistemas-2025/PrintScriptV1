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
