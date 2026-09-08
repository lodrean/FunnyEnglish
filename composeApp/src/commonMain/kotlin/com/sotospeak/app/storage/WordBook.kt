package com.sotospeak.app.storage

import com.sotospeak.shared.platform.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SavedWord(
    val word: String,
    val context: String,          // предложение из транскрипта, где слово встретилось
    val savedAtEpochMs: Long
)

/**
 * Личный словарь ученика (bd FunnyEnglish-h3l.13): локальные сохранённые слова
 * из транскриптов — JSON в [Settings] (паттерн R3, как [RecordingStore]).
 * Перевод появится после подключения внешнего словарного API (bd-остаток).
 * Все вызовы — с главного потока (как RecordingStore).
 */
class WordBook(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }
    private var cache: List<SavedWord>? = null

    fun contains(word: String): Boolean =
        normalized(word) in savedNormalized()

    fun list(): List<SavedWord> = loadAll()

    /** true — слово добавлено, false — уже было. */
    fun add(word: String, context: String, nowEpochMs: Long): Boolean {
        val normalized = normalized(word)
        if (normalized.isEmpty() || normalized in savedNormalized()) return false
        val updated = loadAll() + SavedWord(
            word = normalized,
            context = context,
            savedAtEpochMs = nowEpochMs
        )
        saveAll(updated)
        return true
    }

    fun remove(word: String) {
        val normalized = normalized(word)
        saveAll(loadAll().filterNot { it.word == normalized })
    }

    private fun savedNormalized(): Set<String> = loadAll().asSequence().map { it.word }.toSet()

    private fun normalized(word: String): String =
        word.trim().trim('.', ',', '!', '?', ';', ':', '"', '\'').lowercase()

    private fun loadAll(): List<SavedWord> {
        cache?.let { return it }
        val raw = settings.getString(KEY_WORDS, null)
        val parsed = if (raw.isNullOrEmpty()) emptyList() else try {
            json.decodeFromString<List<SavedWord>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
        cache = parsed
        return parsed
    }

    private fun saveAll(list: List<SavedWord>) {
        cache = list
        settings.putString(KEY_WORDS, json.encodeToString(list))
    }

    private companion object {
        const val KEY_WORDS = "wordbook_words"
    }
}
