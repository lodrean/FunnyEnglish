package com.sotospeak.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.di.API_BASE_URL_OVERRIDE_KEY
import com.sotospeak.app.di.AppConfig
import com.sotospeak.app.util.LogUploader
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.shared.platform.Settings
import com.sotospeak.shared.platform.getPlatformName
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch

/**
 * Скрытое debug-меню (debug/qa-сборки, AppConfig.debugToolsEnabled).
 * Вход: 7 тапов по версии внизу профиля. См. docs/TESTING_ON_LAN.md.
 *
 * Смена base URL применяется сразу: AppConfig.baseUrl читается из Settings
 * перед каждым запросом.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScreen(
    appConfig: AppConfig,
    logUploader: LogUploader,
    onBack: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val scope = rememberCoroutineScope()
    val settings = remember { Settings("sotospeak.preferences") }

    var savedOverride by remember {
        mutableStateOf(settings.getString(API_BASE_URL_OVERRIDE_KEY, null))
    }
    var inputUrl by remember { mutableStateOf(savedOverride ?: appConfig.baseUrl) }
    var status by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }
    var pendingLogs by remember { mutableStateOf(logUploader.pendingCount()) }

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            TopAppBar(
                title = { Text("Debug Menu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
            )
        },
        modifier = Modifier.testTag("debug_menu_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Информация о сборке
            Card(
                shape = SpeakingShapes.Card,
                colors = CardDefaults.cardColors(containerColor = speaking.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DebugRow("Версия", appConfig.appVersion)
                    DebugRow("Платформа", getPlatformName())
                    DebugRow("Effective base URL", appConfig.baseUrl)
                    DebugRow("Источник URL", if (savedOverride != null) "override (debug menu)" else "BuildConfig")
                }
            }

            // Смена backend URL
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("Backend URL (http://<LAN-IP>:8080/)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debug_url_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        settings.putString(API_BASE_URL_OVERRIDE_KEY, inputUrl.trim())
                        savedOverride = inputUrl.trim()
                        status = "Сохранено. Следующий запрос пойдёт на новый URL."
                    },
                    modifier = Modifier.testTag("debug_save_button")
                ) { Text("Сохранить") }

                OutlinedButton(
                    onClick = {
                        settings.remove(API_BASE_URL_OVERRIDE_KEY)
                        savedOverride = null
                        inputUrl = appConfig.baseUrl
                        status = "Override сброшен. Следующий запрос пойдёт на URL из BuildConfig."
                    },
                    modifier = Modifier.testTag("debug_reset_button")
                ) { Text("Сбросить") }
            }

            // Проверка соединения — ad-hoc клиент (НЕ DI-single: проверяем ВВЕДЁННЫЙ url)
            Button(
                onClick = {
                    checking = true
                    status = null
                    scope.launch {
                        status = checkBackendHealth(inputUrl.trim())
                        checking = false
                    }
                },
                enabled = !checking,
                modifier = Modifier.testTag("debug_check_button")
            ) { Text(if (checking) "Проверка…" else "Проверить соединение") }

            // Логи
            OutlinedButton(
                onClick = {
                    scope.launch {
                        logUploader.flush()
                        pendingLogs = logUploader.pendingCount()
                        status = "Логи отправлены (осталось в очереди: $pendingLogs)"
                    }
                },
                modifier = Modifier.testTag("debug_flush_logs_button")
            ) { Text("Отправить логи (в очереди: $pendingLogs)") }

            status?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted,
                    modifier = Modifier.testTag("debug_status")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    val speaking = LocalSpeakingColors.current
    Row {
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = speaking.text
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = speaking.textMuted
        )
    }
}

/** GET {url}/api/actuator/health временным клиентом; человеческий результат */
private suspend fun checkBackendHealth(baseUrl: String): String {
    val url = baseUrl.trimEnd('/') + "/api/actuator/health"
    val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 5_000
            connectTimeoutMillis = 3_000
        }
    }
    return try {
        val response = client.get(url)
        if (response.status.isSuccess() && response.bodyAsText().contains("UP")) {
            "✅ Backend доступен: $url"
        } else {
            "⚠️ Ответ ${response.status.value} без статуса UP ($url)"
        }
    } catch (e: Exception) {
        "❌ Не удалось подключиться: ${e.message ?: "unknown error"}"
    } finally {
        client.close()
    }
}
