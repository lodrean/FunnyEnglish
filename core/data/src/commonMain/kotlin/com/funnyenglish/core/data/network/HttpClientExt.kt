package com.funnyenglish.core.data.network

import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * Executes a network [block] and maps common failure modes to typed [DataError.Network].
 *
 * @return [Result.Success] with the parsed body, or [Result.Error] with a [DataError.Network].
 */
suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Failure(DataError.Network.NO_INTERNET)
    } catch (e: SocketTimeoutException) {
        return Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SerializationException) {
        return Result.Failure(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        return Result.Failure(DataError.Network.UNKNOWN)
    }

    return responseToResult(response)
}

/**
 * Converts an [HttpResponse] into a typed [Result] based on its status code.
 */
suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Result.Failure(DataError.Network.SERIALIZATION)
            } catch (e: SerializationException) {
                Result.Failure(DataError.Network.SERIALIZATION)
            }
        }
        401 -> Result.Failure(DataError.Network.UNAUTHORIZED)
        408 -> Result.Failure(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Failure(DataError.Network.CONFLICT)
        413 -> Result.Failure(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Failure(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Failure(DataError.Network.SERVER_ERROR)
        else -> Result.Failure(DataError.Network.UNKNOWN)
    }
}
