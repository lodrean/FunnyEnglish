package com.funnyenglish.shared.repository

import com.funnyenglish.shared.model.GuestEventDto
import com.funnyenglish.shared.model.GuestSession
import com.funnyenglish.shared.model.GuestTestProgress
import com.funnyenglish.shared.platform.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface GuestProgressRepository {
    fun getSession(): GuestSession?
    fun saveSession(session: GuestSession)
    fun clearSession()
    fun addTestProgress(progress: GuestTestProgress)
    fun getTestProgress(testId: String): GuestTestProgress?
    fun hasProgress(): Boolean

    /** Очередь обезличенных событий для анонимной аналитики */
    fun enqueueEvent(event: GuestEventDto)
    fun getPendingEvents(): List<GuestEventDto>
    fun clearPendingEvents()
    /** anonymousId текущей гостевой сессии (guestId), null если не гость */
    fun getAnonymousId(): String? = getSession()?.guestId
}

class GuestProgressRepositoryImpl(
    private val settings: Settings
) : GuestProgressRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getSession(): GuestSession? {
        val data = settings.getString(KEY_SESSION, null) ?: return null
        return runCatching { json.decodeFromString<GuestSession>(data) }.getOrNull()
    }

    override fun saveSession(session: GuestSession) {
        settings.putString(KEY_SESSION, json.encodeToString(session))
    }

    override fun clearSession() {
        settings.remove(KEY_SESSION)
    }

    override fun addTestProgress(progress: GuestTestProgress) {
        val session = getSession() ?: return
        val updated = session.testProgress.toMutableList().apply {
            removeAll { it.testId == progress.testId }
            add(progress)
        }
        saveSession(session.copy(testProgress = updated))
    }

    override fun getTestProgress(testId: String): GuestTestProgress? {
        return getSession()?.testProgress?.find { it.testId == testId }
    }

    override fun hasProgress(): Boolean {
        return getSession()?.testProgress?.isNotEmpty() == true
    }

    override fun enqueueEvent(event: GuestEventDto) {
        val queue = getPendingEvents().toMutableList().apply { add(event) }
        // Ограничиваем очередь, чтобы не разрасталась в оффлайне
        if (queue.size > MAX_PENDING_EVENTS) queue.removeAt(0)
        settings.putString(KEY_EVENTS, json.encodeToString(queue))
    }

    override fun getPendingEvents(): List<GuestEventDto> {
        val data = settings.getString(KEY_EVENTS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<GuestEventDto>>(data) }.getOrElse { emptyList() }
    }

    override fun clearPendingEvents() {
        settings.remove(KEY_EVENTS)
    }

    companion object {
        private const val KEY_SESSION = "guest_session"
        private const val KEY_EVENTS = "guest_pending_events"
        private const val MAX_PENDING_EVENTS = 100
    }
}
