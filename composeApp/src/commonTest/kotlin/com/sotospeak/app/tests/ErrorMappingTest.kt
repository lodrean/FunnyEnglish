package com.sotospeak.app.tests

import com.sotospeak.app.error.UiText
import com.sotospeak.app.error.asString
import com.sotospeak.app.error.toUiText
import com.sotospeak.shared.api.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Маппинг ApiException → UiText (bd FunnyEnglish-5tf.6): единственная точка перевода
 * технических сообщений в пользовательские (замена userFriendlyError, грабля №15).
 */
class ErrorMappingTest {

    @Test
    fun networkFailureMapsToNoConnection() {
        assertIs<UiText.NoConnection>(
            ApiException(0, "Unable to resolve host \"10.0.2.2\"").toUiText()
        )
        assertIs<UiText.NoConnection>(ApiException(0, "Connection refused").toUiText())
        assertIs<UiText.NoConnection>(ApiException(0, "request timeout").toUiText())
    }

    @Test
    fun deserializationFailureMapsToUnknown() {
        // Сырые JsonException/дампы не показываем пользователю
        assertIs<UiText.Unknown>(ApiException(0, "Field 'newBestScore' is required").toUiText())
    }

    @Test
    fun invalidCredentialsKeepsBackendMessage() {
        val ui = ApiException(401, "Invalid email or password", "INVALID_CREDENTIALS").toUiText()
        assertEquals(UiText.Message("Invalid email or password"), ui)
    }

    @Test
    fun expiredTokenMapsToSessionExpired() {
        assertIs<UiText.SessionExpired>(ApiException(401, "Token expired", "TOKEN_EXPIRED").toUiText())
    }

    @Test
    fun httpStatusesMapToTypedErrors() {
        assertIs<UiText.Forbidden>(ApiException(403, "Forbidden").toUiText())
        assertIs<UiText.NotFound>(ApiException(404, "Not found").toUiText())
        assertIs<UiText.ServerUnavailable>(ApiException(503, "Service Unavailable").toUiText())
        assertIs<UiText.ServerUnavailable>(ApiException(502, "Bad Gateway").toUiText())
    }

    @Test
    fun structuredClientErrorKeepsBackendMessage() {
        val ui = ApiException(400, "Email уже занят", "VALIDATION_ERROR").toUiText()
        assertEquals(UiText.Message("Email уже занят"), ui)
    }

    @Test
    fun unstructuredClientErrorMapsToUnknown() {
        // Тело не ErrorResponse (HTML прокси и т.п.) — сырьё не показываем
        assertIs<UiText.Unknown>(ApiException(400, "<html>Bad Request</html>").toUiText())
    }

    @Test
    fun nonApiExceptionMapsToUnknown() {
        assertIs<UiText.Unknown>(IllegalStateException("boom").toUiText())
    }

    @Test
    fun asStringReturnsUserFriendlyText() {
        assertEquals("Нет соединения с сервером. Проверьте интернет.", UiText.NoConnection.asString())
        assertEquals("Сервер временно недоступен. Попробуйте позже.", UiText.ServerUnavailable.asString())
        assertEquals("Сессия истекла. Войдите снова.", UiText.SessionExpired.asString())
        assertTrue(UiText.Unknown.asString().isNotBlank())
        assertEquals("custom", UiText.Message("custom").asString())
    }
}
