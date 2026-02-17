package com.yourdocs.ui.folder

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.billing.BillingEvent
import com.yourdocs.data.billing.BillingManager
import com.yourdocs.data.billing.PremiumRepository
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.model.FreeLimitReachedException
import com.yourdocs.domain.model.SortPreference
import com.yourdocs.domain.model.Tag
import com.yourdocs.domain.model.UriMetadata
import com.yourdocs.domain.repository.DocumentRepository
import com.yourdocs.domain.repository.FolderRepository
import com.yourdocs.domain.repository.TagRepository
import com.yourdocs.domain.usecase.DeleteDocumentUseCase
import com.yourdocs.domain.usecase.DeleteMultipleDocumentsUseCase
import com.yourdocs.domain.usecase.FindDuplicateDocumentUseCase
import com.yourdocs.domain.usecase.GetAllFoldersUseCase
import com.yourdocs.domain.usecase.GetDocumentsInFolderUseCase
import com.yourdocs.domain.usecase.ImportDocumentUseCase
import com.yourdocs.domain.usecase.MoveMultipleDocumentsUseCase
import com.yourdocs.domain.usecase.RenameDocumentUseCase
import com.yourdocs.domain.usecase.SetExpiryDateUseCase
import com.yourdocs.domain.usecase.SetFolderSortUseCase
import com.yourdocs.domain.usecase.ToggleFavoriteUseCase
import com.yourdocs.domain.usecase.UpdateDocumentNotesUseCase
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val documentRepository: DocumentRepository,
    private val tagRepository: TagRepository,
    private val getDocumentsInFolderUseCase: GetDocumentsInFolderUseCase,
    private val importDocumentUseCase: ImportDocumentUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val renameDocumentUseCase: RenameDocumentUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val updateDocumentNotesUseCase: UpdateDocumentNotesUseCase,
    private val setExpiryDateUseCase: SetExpiryDateUseCase,
    private val setFolderSortUseCase: SetFolderSortUseCase,
    private val findDuplicateDocumentUseCase: FindDuplicateDocumentUseCase,
    private val deleteMultipleDocumentsUseCase: DeleteMultipleDocumentsUseCase,
    private val moveMultipleDocumentsUseCase: MoveMultipleDocumentsUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val premiumRepository: PremiumRepository,
    val billingManager: BillingManager
) : ViewModel() {

    val folderId: String = savedStateHandle.get<String>("folderId")!!

    private val _uiState = MutableStateFlow<FolderDetailUiState>(FolderDetailUiState.Loading)
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    private val _folderName = MutableStateFlow("")
    val folderName: StateFlow<String> = _folderName.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importProgress = MutableStateFlow("")
    val importProgress: StateFlow<String> = _importProgress.asStateFlow()

    private val _showRenameDialog = MutableStateFlow<Document?>(null)
    val showRenameDialog: StateFlow<Document?> = _showRenameDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<Document?>(null)
    val showDeleteDialog: StateFlow<Document?> = _showDeleteDialog.asStateFlow()

    private val _allDocuments = MutableStateFlow<List<Document>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _events = MutableSharedFlow<FolderDetailEvent>()
    val events: SharedFlow<FolderDetailEvent> = _events.asSharedFlow()

    val billingEvents: SharedFlow<BillingEvent> = billingManager.billingEvents

    // Sort preference
    private val _sortPreference = MutableStateFlow<SortPreference?>(null)
    val sortPreference: StateFlow<SortPreference?> = _sortPreference.asStateFlow()

    // Notes dialog
    private val _showNotesDialog = MutableStateFlow<Document?>(null)
    val showNotesDialog: StateFlow<Document?> = _showNotesDialog.asStateFlow()

    // Selection mode (multi-select)
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    // Duplicate detection
    private val _pendingDuplicate = MutableStateFlow<PendingDuplicate?>(null)
    val pendingDuplicate: StateFlow<PendingDuplicate?> = _pendingDuplicate.asStateFlow()

    // Tag management
    private val _showTagDialog = MutableStateFlow<Document?>(null)
    val showTagDialog: StateFlow<Document?> = _showTagDialog.asStateFlow()

    private val _documentTags = MutableStateFlow<Map<String, List<Tag>>>(emptyMap())
    val documentTags: StateFlow<Map<String, List<Tag>>> = _documentTags.asStateFlow()

    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags.asStateFlow()

    // Premium
    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // All folders (for folder picker)
    private val _allFolders = MutableStateFlow<List<Folder>>(emptyList())
    val allFolders: StateFlow<List<Folder>> = _allFolders.asStateFlow()

    // Expiry dialog
    private val _showExpiryDialog = MutableStateFlow<Document?>(null)
    val showExpiryDialog: StateFlow<Document?> = _showExpiryDialog.asStateFlow()

    // Move dialog
    private val _showMoveDialog = MutableStateFlow(false)
    val showMoveDialog: StateFlow<Boolean> = _showMoveDialog.asStateFlow()

    init {
        loadFolderName()
        loadDocuments()
        loadTags()
        loadAllFolders()

        viewModelScope.launch {
            combine(_allDocuments, _searchQuery, _sortPreference) { documents, query, sort ->
                val filtered = if (query.isBlank()) documents
                else documents.filter { it.originalName.contains(query, ignoreCase = true) }

                val sorted = applySorting(filtered, sort)

                if (sorted.isEmpty() && query.isBlank() && documents.isEmpty()) {
                    FolderDetailUiState.Empty
                } else {
                    FolderDetailUiState.Success(sorted)
                }
            }.collect { _uiState.value = it }
        }
    }

    private fun applySorting(docs: List<Document>, sort: SortPreference?): List<Document> {
        return when (sort) {
            SortPreference.NAME_ASC -> docs.sortedBy { it.originalName.lowercase() }
            SortPreference.NAME_DESC -> docs.sortedByDescending { it.originalName.lowercase() }
            SortPreference.DATE_NEWEST -> docs.sortedByDescending { it.createdAt }
            SortPreference.DATE_OLDEST -> docs.sortedBy { it.createdAt }
            SortPreference.SIZE_LARGEST -> docs.sortedByDescending { it.sizeBytes }
            SortPreference.SIZE_SMALLEST -> docs.sortedBy { it.sizeBytes }
            SortPreference.TYPE -> docs.sortedWith(compareBy({ it.mimeType }, { it.originalName.lowercase() }))
            null -> docs
        }
    }

    private fun loadFolderName() {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            _folderName.value = folder?.name ?: "Folder"
            _sortPreference.value = folder?.sortPreference
        }
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            getDocumentsInFolderUseCase(folderId)
                .catch { error ->
                    _uiState.value = FolderDetailUiState.Error(
                        error.message ?: "Failed to load documents"
                    )
                }
                .collect { documents ->
                    _allDocuments.value = documents
                    // Load tags for each document
                    documents.forEach { doc ->
                        loadTagsForDocument(doc.id)
                    }
                }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.observeAllTags().collect { _allTags.value = it }
        }
    }

    private fun loadAllFolders() {
        viewModelScope.launch {
            getAllFoldersUseCase().collect { _allFolders.value = it }
        }
    }

    private fun loadTagsForDocument(documentId: String) {
        viewModelScope.launch {
            tagRepository.observeTagsForDocument(documentId).collect { tags ->
                _documentTags.value = _documentTags.value + (documentId to tags)
            }
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun onImportFromFiles() {
        viewModelScope.launch { _events.emit(FolderDetailEvent.LaunchFilePicker) }
    }

    fun onImportFromGallery() {
        viewModelScope.launch { _events.emit(FolderDetailEvent.LaunchGalleryPicker) }
    }

    fun onFileSelected(uri: Uri, source: DocumentSource) {
        viewModelScope.launch {
            _isImporting.value = true
            _importProgress.value = "Checking for duplicates..."

            // Duplicate detection
            val metadata = documentRepository.resolveUriMetadata(uri)
            val duplicate = findDuplicateDocumentUseCase(folderId, metadata.displayName, metadata.sizeBytes)

            if (duplicate != null) {
                _pendingDuplicate.value = PendingDuplicate(
                    existingDocument = duplicate,
                    uri = uri,
                    source = source,
                    metadata = metadata
                )
                _isImporting.value = false
                return@launch
            }

            _importProgress.value = "Importing document..."
            importDocumentUseCase(folderId, uri, source)
                .onFailure { error ->
                    if (error is FreeLimitReachedException) {
                        _events.emit(FolderDetailEvent.ShowUpgradeSheet("Unlimited Documents"))
                    } else {
                        _events.emit(FolderDetailEvent.ShowError(error.message ?: "Failed to import document"))
                    }
                }
            _isImporting.value = false
        }
    }

    fun onMultipleFilesSelected(uris: List<Uri>, source: DocumentSource) {
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            onFileSelected(uris.first(), source)
            return
        }
        viewModelScope.launch {
            _isImporting.value = true
            var failCount = 0
            var limitReached = false
            uris.forEachIndexed { index, uri ->
                if (limitReached) { failCount++; return@forEachIndexed }
                _importProgress.value = "Importing ${index + 1} of ${uris.size}..."
                importDocumentUseCase(folderId, uri, source)
                    .onFailure { error ->
                        if (error is FreeLimitReachedException) limitReached = true
                        failCount++
                    }
            }
            _isImporting.value = false
            if (limitReached) {
                _events.emit(FolderDetailEvent.ShowUpgradeSheet("Unlimited Documents"))
            } else if (failCount > 0) {
                _events.emit(FolderDetailEvent.ShowError("Failed to import $failCount of ${uris.size} documents"))
            }
        }
    }

    fun onScanCompleted(pdfUri: Uri, pageCount: Int) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss"))
        val displayName = "Scan $timestamp.pdf"

        viewModelScope.launch {
            _isImporting.value = true
            importDocumentUseCase(
                folderId = folderId, uri = pdfUri, source = DocumentSource.CAMERA,
                overrideName = displayName, pageCount = pageCount
            ).onFailure { error ->
                if (error is FreeLimitReachedException) {
                    _events.emit(FolderDetailEvent.ShowUpgradeSheet("Unlimited Documents"))
                } else {
                    _events.emit(FolderDetailEvent.ShowError(error.message ?: "Failed to save scanned document"))
                }
            }
            _isImporting.value = false
        }
    }

    fun onDocumentClick(document: Document) {
        viewModelScope.launch { _events.emit(FolderDetailEvent.NavigateToViewer(document.id)) }
    }

    // Rename
    fun showRenameDialog(document: Document) { _showRenameDialog.value = document }
    fun hideRenameDialog() { _showRenameDialog.value = null }
    fun renameDocument(documentId: String, newName: String) {
        viewModelScope.launch {
            renameDocumentUseCase(documentId, newName)
                .onSuccess { hideRenameDialog() }
                .onFailure { _events.emit(FolderDetailEvent.ShowError(it.message ?: "Failed to rename document")) }
        }
    }

    // Delete
    fun showDeleteDialog(document: Document) { _showDeleteDialog.value = document }
    fun hideDeleteDialog() { _showDeleteDialog.value = null }
    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            deleteDocumentUseCase(documentId)
                .onSuccess { hideDeleteDialog() }
                .onFailure { _events.emit(FolderDetailEvent.ShowError(it.message ?: "Failed to delete document")) }
        }
    }

    // Favorites
    fun toggleFavorite(documentId: String) {
        viewModelScope.launch { toggleFavoriteUseCase(documentId) }
    }

    // Notes
    fun showNotesDialog(document: Document) { _showNotesDialog.value = document }
    fun hideNotesDialog() { _showNotesDialog.value = null }
    fun updateDocumentNotes(documentId: String, notes: String?) {
        viewModelScope.launch {
            updateDocumentNotesUseCase(documentId, notes)
            hideNotesDialog()
        }
    }

    // Sorting
    fun setSortPreference(sort: SortPreference) {
        _sortPreference.value = sort
        viewModelScope.launch { setFolderSortUseCase(folderId, sort) }
    }

    // Expiry
    fun showExpiryDialog(document: Document) {
        if (!isPremium.value) {
            viewModelScope.launch { _events.emit(FolderDetailEvent.ShowUpgradeSheet("Expiry Reminders")) }
            return
        }
        _showExpiryDialog.value = document
    }
    fun hideExpiryDialog() { _showExpiryDialog.value = null }
    fun setExpiryDate(documentId: String, date: Instant?) {
        viewModelScope.launch {
            setExpiryDateUseCase(documentId, date)
            hideExpiryDialog()
        }
    }

    // Tags
    fun showTagDialog(document: Document) {
        if (!isPremium.value) {
            viewModelScope.launch { _events.emit(FolderDetailEvent.ShowUpgradeSheet("Document Tags")) }
            return
        }
        _showTagDialog.value = document
    }
    fun hideTagDialog() { _showTagDialog.value = null }

    fun createAndAssignTag(documentId: String, name: String, colorHex: String) {
        viewModelScope.launch {
            tagRepository.createTag(name, colorHex).onSuccess { tag ->
                tagRepository.addTagToDocument(documentId, tag.id)
            }
        }
    }

    fun assignTagToDocument(documentId: String, tagId: String) {
        viewModelScope.launch { tagRepository.addTagToDocument(documentId, tagId) }
    }

    fun removeTagFromDocument(documentId: String, tagId: String) {
        viewModelScope.launch { tagRepository.removeTagFromDocument(documentId, tagId) }
    }

    // Multi-select
    fun enterSelectionMode(firstId: String) {
        if (!isPremium.value) {
            viewModelScope.launch { _events.emit(FolderDetailEvent.ShowUpgradeSheet("Multi-Select")) }
            return
        }
        _isSelectionMode.value = true
        _selectedIds.value = setOf(firstId)
    }

    fun toggleSelection(id: String) {
        val current = _selectedIds.value
        _selectedIds.value = if (id in current) current - id else current + id
        if (_selectedIds.value.isEmpty()) exitSelectionMode()
    }

    fun selectAll() {
        _selectedIds.value = _allDocuments.value.map { it.id }.toSet()
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            deleteMultipleDocumentsUseCase(ids)
            exitSelectionMode()
        }
    }

    fun showMoveDialog() { _showMoveDialog.value = true }
    fun hideMoveDialog() { _showMoveDialog.value = false }

    fun moveSelected(targetFolderId: String) {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            moveMultipleDocumentsUseCase(ids, targetFolderId)
            exitSelectionMode()
            hideMoveDialog()
        }
    }

    // Duplicate handling
    fun onDuplicateReplace(existingId: String, uri: Uri, source: DocumentSource) {
        viewModelScope.launch {
            _pendingDuplicate.value = null
            _isImporting.value = true
            _importProgress.value = "Replacing document..."
            deleteDocumentUseCase(existingId)
            importDocumentUseCase(folderId, uri, source)
            _isImporting.value = false
        }
    }

    fun onDuplicateKeepBoth(uri: Uri, source: DocumentSource, originalName: String) {
        viewModelScope.launch {
            _pendingDuplicate.value = null
            _isImporting.value = true
            _importProgress.value = "Importing document..."
            val nameWithoutExt = originalName.substringBeforeLast('.')
            val ext = originalName.substringAfterLast('.', "")
            val newName = if (ext.isNotEmpty()) "$nameWithoutExt (2).$ext" else "$nameWithoutExt (2)"
            importDocumentUseCase(folderId, uri, source, overrideName = newName)
            _isImporting.value = false
        }
    }

    fun onDuplicateCancel() {
        _pendingDuplicate.value = null
    }
}

data class PendingDuplicate(
    val existingDocument: Document,
    val uri: Uri,
    val source: DocumentSource,
    val metadata: UriMetadata
)

sealed interface FolderDetailUiState {
    data object Loading : FolderDetailUiState
    data object Empty : FolderDetailUiState
    data class Success(val documents: List<Document>) : FolderDetailUiState
    data class Error(val message: String) : FolderDetailUiState
}

sealed interface FolderDetailEvent {
    data object LaunchFilePicker : FolderDetailEvent
    data object LaunchGalleryPicker : FolderDetailEvent
    data class NavigateToViewer(val documentId: String) : FolderDetailEvent
    data class ShowError(val message: String) : FolderDetailEvent
    data class ShowUpgradeSheet(val triggerFeature: String) : FolderDetailEvent
}
