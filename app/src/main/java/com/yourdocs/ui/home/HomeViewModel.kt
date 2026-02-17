package com.yourdocs.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.billing.BillingEvent
import com.yourdocs.data.billing.BillingManager
import com.yourdocs.data.billing.PremiumRepository
import com.yourdocs.data.preferences.UserPreferencesRepository
import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.model.FreeLimitReachedException
import com.yourdocs.domain.model.LockMethod
import com.yourdocs.domain.model.Tag
import com.yourdocs.domain.repository.TagRepository
import com.yourdocs.domain.usecase.CreateFolderUseCase
import com.yourdocs.domain.usecase.DeleteFolderUseCase
import com.yourdocs.domain.usecase.GetAllFoldersUseCase
import com.yourdocs.domain.usecase.GetExpiringSoonUseCase
import com.yourdocs.domain.usecase.GetFavoritesUseCase
import com.yourdocs.domain.usecase.GetRecentlyViewedUseCase
import com.yourdocs.domain.usecase.RenameFolderUseCase
import com.yourdocs.domain.usecase.SearchDocumentsUseCase
import com.yourdocs.domain.usecase.SetFolderLockUseCase
import com.yourdocs.domain.usecase.SetupPinUseCase
import com.yourdocs.domain.usecase.ToggleFolderPinnedUseCase
import com.yourdocs.domain.usecase.UpdateFolderDescriptionUseCase
import com.yourdocs.domain.usecase.VerifyPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
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
    private val setFolderLockUseCase: SetFolderLockUseCase,
    private val setupPinUseCase: SetupPinUseCase,
    private val updateFolderDescriptionUseCase: UpdateFolderDescriptionUseCase,
    private val verifyPinUseCase: VerifyPinUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getRecentlyViewedUseCase: GetRecentlyViewedUseCase,
    private val getExpiringSoonUseCase: GetExpiringSoonUseCase,
    private val searchDocumentsUseCase: SearchDocumentsUseCase,
    private val tagRepository: TagRepository,
    premiumRepository: PremiumRepository,
    val billingManager: BillingManager,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _allFolders = MutableStateFlow<List<Folder>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val biometricEnabled: StateFlow<Boolean> = preferencesRepository.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isPinConfigured: StateFlow<Boolean> = preferencesRepository.isPinConfigured
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _showRenameDialog = MutableStateFlow<Folder?>(null)
    val showRenameDialog: StateFlow<Folder?> = _showRenameDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<Folder?>(null)
    val showDeleteDialog: StateFlow<Folder?> = _showDeleteDialog.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    val billingEvents: SharedFlow<BillingEvent> = billingManager.billingEvents

    private var _pendingAuthAction: PendingAuthAction? = null

    // Favorites
    val favorites: StateFlow<List<DocumentWithFolderInfo>> = getFavoritesUseCase()
        .catch { /* ignore */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recently viewed
    val recentlyViewed: StateFlow<List<DocumentWithFolderInfo>> = getRecentlyViewedUseCase()
        .catch { /* ignore */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expiring soon
    val expiringSoon: StateFlow<List<DocumentWithFolderInfo>> = getExpiringSoonUseCase()
        .catch { /* ignore */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tags
    val allTags: StateFlow<List<Tag>> = tagRepository.observeAllTags()
        .catch { /* ignore */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTagId = MutableStateFlow<String?>(null)
    val selectedTagId: StateFlow<String?> = _selectedTagId.asStateFlow()

    val tagFilteredDocuments: StateFlow<List<DocumentWithFolderInfo>> = _selectedTagId
        .flatMapLatest { tagId ->
            if (tagId != null) tagRepository.observeDocumentsByTag(tagId)
            else emptyFlow()
        }
        .catch { /* ignore */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results (documents)
    val searchResults: StateFlow<List<DocumentWithFolderInfo>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) emptyFlow()
            else searchDocumentsUseCase(query)
        }
        .catch { /* ignore */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadFolders()

        viewModelScope.launch {
            combine(_allFolders, _searchQuery) { folders, query ->
                if (query.isBlank()) {
                    if (folders.isEmpty()) HomeUiState.Empty else HomeUiState.Success(folders)
                } else {
                    val filtered = folders.filter { folder ->
                        folder.name.contains(query, ignoreCase = true) ||
                                (folder.description?.contains(query, ignoreCase = true) == true)
                    }
                    HomeUiState.Success(filtered)
                }
            }.collect { _uiState.value = it }
        }
    }

    private fun loadFolders() {
        viewModelScope.launch {
            getAllFoldersUseCase()
                .catch { error -> _uiState.value = HomeUiState.Error(error.message ?: "Unknown error") }
                .collect { folders -> _allFolders.value = folders }
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun onTagSelected(tagId: String?) {
        _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId
    }

    fun onFolderClick(folder: Folder) {
        if (folder.isLocked && folder.lockMethod != null) {
            _pendingAuthAction = PendingAuthAction.OpenFolder(folder.id)
            viewModelScope.launch {
                _events.emit(HomeEvent.RequestAuthentication(
                    action = PendingAuthAction.OpenFolder(folder.id),
                    lockMethod = folder.lockMethod,
                    title = "Unlock Folder",
                    subtitle = "Authenticate to access this locked folder"
                ))
            }
        } else {
            viewModelScope.launch { _events.emit(HomeEvent.NavigateToFolder(folder.id)) }
        }
    }

    fun onAuthSuccess(action: PendingAuthAction) {
        _pendingAuthAction = null
        viewModelScope.launch {
            when (action) {
                is PendingAuthAction.OpenFolder -> _events.emit(HomeEvent.NavigateToFolder(action.folderId))
                is PendingAuthAction.DeleteFolder -> _showDeleteDialog.value = action.folder
                is PendingAuthAction.RenameFolder -> _showRenameDialog.value = action.folder
                is PendingAuthAction.UnlockFolder -> setFolderLockUseCase(action.folderId, lock = false)
            }
        }
    }

    fun showCreateDialog() { _showCreateDialog.value = true }
    fun hideCreateDialog() { _showCreateDialog.value = false }

    fun createFolder(name: String, colorHex: String? = null, emoji: String? = null, description: String? = null) {
        viewModelScope.launch {
            createFolderUseCase(name, colorHex, emoji, description)
                .onSuccess { hideCreateDialog() }
                .onFailure { error ->
                    if (error is FreeLimitReachedException) {
                        hideCreateDialog()
                        _events.emit(HomeEvent.ShowUpgradeSheet("Unlimited Folders"))
                    }
                }
        }
    }

    fun showRenameDialog(folder: Folder) {
        if (folder.isLocked && folder.lockMethod != null) {
            _pendingAuthAction = PendingAuthAction.RenameFolder(folder)
            viewModelScope.launch {
                _events.emit(HomeEvent.RequestAuthentication(
                    action = PendingAuthAction.RenameFolder(folder),
                    lockMethod = folder.lockMethod,
                    title = "Rename Locked Folder",
                    subtitle = "Authenticate to rename this locked folder"
                ))
            }
        } else { _showRenameDialog.value = folder }
    }
    fun hideRenameDialog() { _showRenameDialog.value = null }

    fun renameFolder(folderId: String, newName: String, description: String? = null) {
        viewModelScope.launch {
            renameFolderUseCase(folderId, newName)
                .onSuccess {
                    val currentFolder = (uiState.value as? HomeUiState.Success)?.folders?.find { it.id == folderId }
                    if (description != currentFolder?.description) {
                        updateFolderDescriptionUseCase(folderId, description)
                    }
                    hideRenameDialog()
                }
        }
    }

    fun showDeleteDialog(folder: Folder) {
        if (folder.isLocked && folder.lockMethod != null) {
            _pendingAuthAction = PendingAuthAction.DeleteFolder(folder)
            viewModelScope.launch {
                _events.emit(HomeEvent.RequestAuthentication(
                    action = PendingAuthAction.DeleteFolder(folder),
                    lockMethod = folder.lockMethod,
                    title = "Delete Locked Folder",
                    subtitle = "Authenticate to delete this locked folder"
                ))
            }
        } else { _showDeleteDialog.value = folder }
    }
    fun hideDeleteDialog() { _showDeleteDialog.value = null }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch { deleteFolderUseCase(folderId).onSuccess { hideDeleteDialog() } }
    }

    fun togglePinned(folderId: String) {
        viewModelScope.launch { toggleFolderPinnedUseCase(folderId) }
    }

    fun toggleLocked(folder: Folder) {
        if (!isPremium.value && !folder.isLocked) {
            viewModelScope.launch { _events.emit(HomeEvent.ShowUpgradeSheet("Folder Locking")) }
            return
        }
        if (folder.isLocked && folder.lockMethod != null) {
            _pendingAuthAction = PendingAuthAction.UnlockFolder(folder.id)
            viewModelScope.launch {
                _events.emit(HomeEvent.RequestAuthentication(
                    action = PendingAuthAction.UnlockFolder(folder.id),
                    lockMethod = folder.lockMethod,
                    title = "Unlock Folder",
                    subtitle = "Authenticate to unlock this folder"
                ))
            }
        } else {
            viewModelScope.launch { _events.emit(HomeEvent.ShowLockMethodPicker(folder.id)) }
        }
    }

    fun onLockMethodChosen(folderId: String, method: LockMethod) {
        val needsPin = method == LockMethod.PIN || method == LockMethod.BOTH
        if (needsPin && !isPinConfigured.value) {
            viewModelScope.launch { _events.emit(HomeEvent.ShowSetupPin(folderId, method)) }
        } else {
            viewModelScope.launch { setFolderLockUseCase(folderId, lock = true, lockMethod = method) }
        }
    }

    fun onPinSetupComplete(folderId: String, method: LockMethod, pin: String) {
        viewModelScope.launch {
            setupPinUseCase(pin).onSuccess {
                setFolderLockUseCase(folderId, lock = true, lockMethod = method)
            }.onFailure { _events.emit(HomeEvent.ShowToast("Failed to set up PIN")) }
        }
    }

    suspend fun verifyPin(pin: String): Boolean = verifyPinUseCase(pin)
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(val folders: List<Folder>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface HomeEvent {
    data class NavigateToFolder(val folderId: String) : HomeEvent
    data class NavigateToDocument(val documentId: String) : HomeEvent
    data class RequestAuthentication(
        val action: PendingAuthAction, val lockMethod: LockMethod, val title: String, val subtitle: String
    ) : HomeEvent
    data class ShowLockMethodPicker(val folderId: String) : HomeEvent
    data class ShowSetupPin(val folderId: String, val method: LockMethod) : HomeEvent
    data class ShowToast(val message: String) : HomeEvent
    data class ShowUpgradeSheet(val triggerFeature: String) : HomeEvent
}

sealed interface PendingAuthAction {
    data class OpenFolder(val folderId: String) : PendingAuthAction
    data class DeleteFolder(val folder: Folder) : PendingAuthAction
    data class RenameFolder(val folder: Folder) : PendingAuthAction
    data class UnlockFolder(val folderId: String) : PendingAuthAction
}
