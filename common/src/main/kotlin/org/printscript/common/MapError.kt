package org.printscript.common

inline fun <T, E, F> Result<T, E>.mapError(
    crossinline transform: (E) -> F,
): Result<T, F> =
    this.fold(
        onSuccess = { value -> Success(value) },
        onFailure = { err -> Failure(transform(err)) },
    )
