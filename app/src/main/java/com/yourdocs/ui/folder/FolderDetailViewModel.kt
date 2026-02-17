package com.yourdocs.ui.folder

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.billing.BillingEvent
import com.yourdocs.data.billing.BillingManager
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.model.FreeLimitReachedException
import com.yourdocs.domain.repository.FolderRepository
import com.yourdocs.domain.usecase.DeleteDocumentUseCase
import com.yourdocs.domain.usecase.GetDocumentsInFolderUseCase
import com.yourdocs.domain.usecase.ImportDocumentUseCase
import com.yourdocs.domain.usecase.RenameDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val getDocumentsInFolderUseCase: GetDocumentsInFolderUseCase,
    private val importDocumentUseCase: ImportDocumentUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val renameDocumentUseCase: RenameDocumentUseCase,
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

    private val _events = MutableSharedFlow<FolderDetailEvent>()
    val events: SharedFlow<FolderDetailEvent> = _events.asSharedFlow()

    val billingEvents: SharedFlow<BillingEvent> = billingManager.billingEvents

    init {
        loadFolderName()
        loadDocuments()
    }

    private fun loadFolderName() {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            _folderName.value = folder?.name ?: "Folder"
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
                    _uiState.value = if (documents.isEmpty()) {
                        FolderDetailUiState.Empty
                    } else {
                        FolderDetailUiState.Success(documents)
                    }
                }
        }
    }

    fun onImportFromFiles() {
        viewModelScope.launch {
            _events.emit(FolderDetailEvent.LaunchFilePicker)
        }
    }

    fun onImportFromGallery() {
        viewModelScope.launch {
            _events.emit(FolderDetailEvent.LaunchGalleryPicker)
        }
    }

    fun onFileSelected(uri: Uri, source: DocumentSource) {
        viewModelScope.launch {
            _isImporting.value = true
            _importProgress.value = "Importing document..."
            importDocumentUseCase(folderId, uri, source)
                .onFailure { error ->
                    if (error is FreeLimitReachedException) {
                        _events.emit(FolderDetailEvent.ShowUpgradeSheet("Unlimited Documents"))
                    } else {
                        _events.emit(
                            FolderDetailEvent.ShowError(
                                error.message ?: "Failed to import document"
                            )
                        )
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
                if (limitReached) {
                    failCount++
                    return@forEachIndexed
                }
                _importProgress.value = "Importing ${index + 1} of ${uris.size}..."
                importDocumentUseCase(folderId, uri, source)
                    .onFailure { error ->
                        if (error is FreeLimitReachedException) {
                            limitReached = true
                        }
                        failCount++
                    }
            }
            _isImporting.value = false
            if (limitReached) {
                _events.emit(FolderDetailEvent.ShowUpgradeSheet("Unlimited Documents"))
            } else if (failCount > 0) {
                _events.emit(
                    FolderDetailEvent.ShowError("Failed to import $failCount of ${uris.size} documents")
                )
            }
        }
    }

    fun onScanCompleted(pdfUri: Uri, pageCount: Int) {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss"))
        val displayName = "Scan $timestamp.pdf"

        viewModelScope.launch {
            _isImporting.value = true
            importDocumentUseCase(
                folderId = folderId,
                uri = pdfUri,
                source = DocumentSource.CAMERA,
                overrideName = displayName,
                pageCount = pageCount
            ).onFailure { error ->
                if (error is FreeLimitReachedException) {
                    _events.emit(FolderDetailEvent.ShowUpgradeSheet("Unlimited Documents"))
                } else {
                    _events.emit(
                        FolderDetailEvent.ShowError(
                            error.message ?: "Failed to save scanned document"
                        )
                    )
                }
            }
            _isImporting.value = false
        }
    }

    fun onDocumentClick(document: Document) {
        viewModelScope.launch {
            _events.emit(FolderDetailEvent.NavigateToViewer(document.id))
        }
    }

    fun showRenameDialog(document: Document) {
        _showRenameDialog.value = document
    }

    fun hideRenameDialog() {
        _showRenameDialog.value = null
    }

    fun renameDocument(documentId: String, newName: String) {
        viewModelScope.launch {
            renameDocumentUseCase(documentId, newName)
                .onSuccess {
                    hideRenameDialog()
                }
                .onFailure { error ->
                    _events.emit(
                        FolderDetailEvent.ShowError(
                            error.message ?: "Failed to rename document"
                        )
                    )
                }
        }
    }

    fun showDeleteDialog(document: Document) {
        _showDeleteDialog.value = document
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = null
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            deleteDocumentUseCase(documentId)
                .onSuccess {
                    hideDeleteDialog()
                }
                .onFailure { error ->
                    _events.emit(
                        FolderDetailEvent.ShowError(
                            error.message ?: "Failed to delete document"
                        )
                    )
                }
        }
    }
}

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
