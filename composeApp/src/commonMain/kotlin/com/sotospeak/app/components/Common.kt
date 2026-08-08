package com.sotospeak.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Упс!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userFriendlyError(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Попробовать снова")
            }
        }
    }
}

/**
 * Маппит технические сообщения об ошибках (дампы Ktor-исключений, URL, статус-коды)
 * в понятный пользователю текст. Сырые exception.message в UI не показываем.
 */
private fun userFriendlyError(raw: String): String {
    val lower = raw.lowercase()
    return when {
        "502" in lower || "503" in lower || "504" in lower || "proxy error" in lower ->
            "Сервер временно недоступен. Попробуйте позже."
        "unable to resolve host" in lower || "connection refused" in lower ||
            "failed to connect" in lower || "timeout" in lower ->
            "Нет соединения с сервером. Проверьте интернет."
        "401" in lower -> "Сессия истекла. Войдите снова."
        "403" in lower -> "Нет доступа к этим данным."
        "404" in lower -> "Данные не найдены."
        "notransformationfound" in lower || "expected response body" in lower ||
            "kotlin reflection" in lower || raw.length > 200 ->
            "Не удалось загрузить данные. Попробуйте ещё раз."
        else -> raw
    }
}
