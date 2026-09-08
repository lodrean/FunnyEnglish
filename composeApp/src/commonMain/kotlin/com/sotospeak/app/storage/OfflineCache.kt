package com.sotospeak.app.storage

import com.sotospeak.shared.platform.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Офлайн-кэш последних успешных ответов (bd FunnyEnglish-h3l.8, §4.1.3):
 * JSON-снапшоты в [Settings]; при сетевой ошибке VM показывает сохранённые
 * данные вместо пустого экрана. Только чтение кэша — мутации всегда через API.
 */
class OfflineCache(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        try {
            settings.putString(key, json.encodeToString(serializer, value))
        } catch (e: Exception) {
            // кэш не критичен — ошибка записи не должна ломать основной флоу
        }
    }

    fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? = try {
        settings.getString(key, null)?.let { json.decodeFromString(serializer, it) }
    } catch (e: Exception) {
        null
    }

    companion object {
        const val KEY_LIBRARIES = "offline_cache_libraries"
        const val KEY_TOPICS_PREFIX = "offline_cache_topics_"
        const val KEY_SUBMISSIONS = "offline_cache_submissions"
    }
}
