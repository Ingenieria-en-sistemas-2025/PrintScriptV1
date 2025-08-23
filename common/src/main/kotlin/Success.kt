data class Success<T>(val value: T): Result<T, Nothing> {
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