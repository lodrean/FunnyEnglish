package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.shared.api.MessagingApi
import com.sotospeak.shared.contracts.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MessagesState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val error: String? = null
)

/**
 * Inbox ученика: сообщения и комментарии от учителя.
 */
class MessagesViewModel(
    private val messagingApi: MessagingApi
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesState())
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun loadUnreadCount() {
        viewModelScope.launch {
            messagingApi.getUnreadMessagesCount()
                .onSuccess { _unreadCount.value = it.count.toInt() }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            messagingApi.getMessages()
                .onSuccess { messages ->
                    _state.value = MessagesState(isLoading = false, messages = messages)
                }
                .onFailure { error ->
                    _state.value = MessagesState(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить сообщения"
                    )
                }
        }
    }

    fun markAsRead(messageId: String) {
        // Оптимистично помечаем локально
        _state.value = _state.value.copy(
            messages = _state.value.messages.map {
                if (it.id == messageId && it.readAt == null) {
                    it.copy(readAt = "read")
                } else it
            }
        )
        viewModelScope.launch {
            messagingApi.markMessageAsRead(messageId)
        }
    }
}
