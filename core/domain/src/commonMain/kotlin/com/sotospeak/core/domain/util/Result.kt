package com.sotospeak.core.domain.util

/**
 * A typed Result wrapper that models success and failure explicitly.
 *
 * @param D The success data type.
 * @param E The error type, must implement [DomainError].
 */
sealed interface Result<out D, out E : DomainError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Failure<out E : DomainError>(val error: E) : Result<Nothing, E>
}

/**
 * Maps the success value of a [Result] to another type.
 */
inline fun <T, E : DomainError, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Failure -> Result.Failure(error)
        is Result.Success -> Result.Success(map(data))
    }
}

/**
 * Converts a successful result into an [EmptyResult] discarding the data.
 */
fun <T, E : DomainError> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map { }
}

/**
 * Typealias for a [Result] that returns [Unit] on success.
 */
typealias EmptyResult<E> = Result<Unit, E>

/**
 * Invokes the given [action] if this [Result] is a success.
 */
inline fun <T, E : DomainError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Failure -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

/**
 * Invokes the given [action] if this [Result] is an error.
 */
inline fun <T, E : DomainError> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Failure -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}
