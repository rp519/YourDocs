package com.yourdocs.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.preferences.UserPreferencesRepository
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.usecase.CreateFolderUseCase
import com.yourdocs.domain.usecase.DeleteFolderUseCase
import com.yourdocs.domain.usecase.GetAllFoldersUseCase
import com.yourdocs.domain.usecase.RenameFolderUseCase
import com.yourdocs.domain.usecase.ToggleFolderLockedUseCase
import com.yourdocs.domain.usecase.ToggleFolderPinnedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val toggleFolderPinnedUseCase: ToggleFolderPinnedUseCase,
    private val toggleFolderLockedUseCase: ToggleFolderLockedUseCase,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val biometricEnabled: StateFlow<Boolean> = preferencesRepository.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _showRenameDialog = MutableStateFlow<Folder?>(null)
    val showRenameDialog: StateFlow<Folder?> = _showRenameDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<Folder?>(null)
    val showDeleteDialog: StateFlow<Folder?> = _showDeleteDialog.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            getAllFoldersUseCase()
                .catch { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                }
                .collect { folders ->
                    _uiState.value = if (folders.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success(folders)
                    }
                }
        }
    }

    fun onFolderClick(folder: Folder) {
        if (folder.isLocked && biometricEnabled.value) {
            viewModelScope.launch {
                _events.emit(HomeEvent.RequestBiometric(folder.id))
            }
        } else {
            viewModelScope.launch {
                _events.emit(HomeEvent.NavigateToFolder(folder.id))
            }
        }
    }

    fun onBiometricSuccess(folderId: String) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToFolder(folderId))
        }
    }

    fun showCreateDialog() { _showCreateDialog.value = true }
    fun hideCreateDialog() { _showCreateDialog.value = false }

    fun createFolder(name: String, colorHex: String? = null, emoji: String? = null) {
        viewModelScope.launch {
            createFolderUseCase(name, colorHex, emoji)
                .onSuccess { hideCreateDialog() }
                .onFailure { /* Error handled by UI */ }
        }
    }

    fun showRenameDialog(folder: Folder) { _showRenameDialog.value = folder }
    fun hideRenameDialog() { _showRenameDialog.value = null }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            renameFolderUseCase(folderId, newName)
                .onSuccess { hideRenameDialog() }
                .onFailure { /* Error handled by UI */ }
        }
    }

    fun showDeleteDialog(folder: Folder) { _showDeleteDialog.value = folder }
    fun hideDeleteDialog() { _showDeleteDialog.value = null }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            deleteFolderUseCase(folderId)
                .onSuccess { hideDeleteDialog() }
                .onFailure { /* Error handled by UI */ }
        }
    }

    fun togglePinned(folderId: String) {
        viewModelScope.launch { toggleFolderPinnedUseCase(folderId) }
    }

    fun toggleLocked(folderId: String) {
        viewModelScope.launch { toggleFolderLockedUseCase(folderId) }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(val folders: List<Folder>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface HomeEvent {
    data class NavigateToFolder(val folderId: String) : HomeEvent
    data class RequestBiometric(val folderId: String) : HomeEvent
}
