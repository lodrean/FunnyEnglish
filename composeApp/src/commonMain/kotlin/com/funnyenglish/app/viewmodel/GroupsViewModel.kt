package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.GroupDetail
import com.funnyenglish.shared.model.StudentGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для управления группами ученика
 */

data class GroupsUiState(
    val groups: List<StudentGroup> = emptyList(),
    val selectedGroup: GroupDetail? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val showJoinDialog: Boolean = false,
    val inviteCodeInput: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class GroupsViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState

    /**
     * Загрузить список групп ученика
     */
    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            api.getMyStudentGroups()
                .onSuccess { groups ->
                    _uiState.update { 
                        it.copy(
                            groups = groups,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Не удалось загрузить группы: ${error.message}"
                        )
                    }
                }
        }
    }

    /**
     * Загрузить детали группы
     */
    fun loadGroupDetail(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            api.getStudentGroupDetail(groupId)
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            selectedGroup = detail,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Не удалось загрузить группу: ${error.message}"
                        )
                    }
                }
        }
    }

    /**
     * Показать диалог присоединения к группе
     */
    fun showJoinDialog() {
        _uiState.update { 
            it.copy(
                showJoinDialog = true, 
                inviteCodeInput = "",
                errorMessage = null
            ) 
        }
    }

    /**
     * Скрыть диалог присоединения
     */
    fun hideJoinDialog() {
        _uiState.update { it.copy(showJoinDialog = false, inviteCodeInput = "") }
    }

    /**
     * Обновить код приглашения
     */
    fun onInviteCodeChange(code: String) {
        _uiState.update { it.copy(inviteCodeInput = code.uppercase().trim()) }
    }

    /**
     * Присоединиться к группе по коду
     */
    fun joinGroup() {
        val code = _uiState.value.inviteCodeInput
        if (code.length < 4) return

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }
            
            api.joinGroupByCode(code)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(
                                isJoining = false,
                                showJoinDialog = false,
                                inviteCodeInput = "",
                                successMessage = response.message
                            )
                        }
                        // Reload groups
                        loadGroups()
                    } else {
                        _uiState.update {
                            it.copy(
                                isJoining = false,
                                errorMessage = response.message
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            errorMessage = "Ошибка: ${error.message}"
                        )
                    }
                }
        }
    }

    /**
     * Покинуть группу
     */
    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            api.leaveGroup(groupId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Вы покинули группу",
                            selectedGroup = null
                        )
                    }
                    // Reload groups
                    loadGroups()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Не удалось покинуть группу: ${error.message}"
                        )
                    }
                }
        }
    }

    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Очистить сообщение об успехе
     */
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
