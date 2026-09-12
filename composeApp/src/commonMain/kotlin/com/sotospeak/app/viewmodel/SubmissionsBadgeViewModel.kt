package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.shared.api.TokenProvider
import com.sotospeak.shared.platform.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Счётчик бейджа «Отправки» (редизайн V1 «Путь спикера», OpenSpec app-navigation):
 * число practice-сабмишнов, перешедших в REVIEWED после локальной метки
 * [KEY_LAST_SEEN]. Метка обновляется при открытии вкладки «Отправки» ([markSeen]),
 * бейдж гаснет. Для гостя/офлайна бейдж не показывается (ошибка загрузки = 0).
 */
class SubmissionsBadgeViewModel(
    private val repository: SpeakingRepository,
    private val settings: Settings,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _badgeCount = MutableStateFlow(0)
    val badgeCount: StateFlow<Int> = _badgeCount.asStateFlow()

    init {
        refresh()
    }

    /** Пересчитать бейдж по текущему списку сабмишнов. */
    fun refresh() {
        // Гость: сабмишнов у сервера нет — бейдж гасим БЕЗ запроса, иначе каждый
        // переход экрана даёт 401 в консоль браузера (ловят e2e-cmp critical-errors)
        if (tokenProvider.getToken().isNullOrBlank()) {
            _badgeCount.value = 0
            return
        }
        viewModelScope.launch {
            repository.getMySubmissions().onSuccess { submissions ->
                val lastSeen = settings.getString(KEY_LAST_SEEN, null)
                // Пустая метка (первый запуск) — старые оценки «новыми» не считаем
                _badgeCount.value = if (lastSeen.isNullOrBlank()) {
                    0
                } else {
                    submissions.count { submission ->
                        submission.status == STATUS_REVIEWED &&
                            (submission.grade?.createdAt ?: "") > lastSeen
                    }
                }
            }
        }
    }

    /** Вкладка «Отправки» открыта — новые оценки просмотрены. */
    fun markSeen() {
        settings.putString(KEY_LAST_SEEN, kotlinx.datetime.Clock.System.now().toString())
        _badgeCount.value = 0
    }

    private companion object {
        const val KEY_LAST_SEEN = "submissions_last_seen"
        const val STATUS_REVIEWED = "REVIEWED"
    }
}
