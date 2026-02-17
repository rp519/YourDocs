package com.yourdocs.ui.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.local.storage.FileStorageManager
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.repository.DocumentRepository
import com.yourdocs.domain.usecase.MarkDocumentViewedUseCase
import com.yourdocs.domain.usecase.ToggleFavoriteUseCase
import com.yourdocs.domain.usecase.UpdateDocumentNotesUseCase
import com.yourdocs.domain.usecase.SetExpiryDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import javax.inject.Inject

enum class ViewerType { PDF, IMAGE, TEXT, UNSUPPORTED }

sealed interface ViewerUiState {
    data object Loading : ViewerUiState
    data class Ready(
        val document: Document,
        val file: File,
        val viewerType: ViewerType
    ) : ViewerUiState
    data class Error(val message: String) : ViewerUiState
}

@HiltViewModel
class DocumentViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val fileStorageManager: FileStorageManager,
    private val markDocumentViewedUseCase: MarkDocumentViewedUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val updateDocumentNotesUseCase: UpdateDocumentNotesUseCase,
    private val setExpiryDateUseCase: SetExpiryDateUseCase
) : ViewModel() {

    private val documentId: String = savedStateHandle.get<String>("documentId")!!

    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private val _showNotesDialog = MutableStateFlow(false)
    val showNotesDialog: StateFlow<Boolean> = _showNotesDialog.asStateFlow()

    init {
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            try {
                val document = documentRepository.getDocumentById(documentId)
                if (document == null) {
                    _uiState.value = ViewerUiState.Error("Document not found")
                    return@launch
                }

                val file = fileStorageManager.getDocumentFile(document.storedFileName)
                if (!file.exists()) {
                    _uiState.value = ViewerUiState.Error("File not found on disk")
                    return@launch
                }

                val viewerType = resolveViewerType(document.mimeType)
                _uiState.value = ViewerUiState.Ready(document, file, viewerType)

                // Mark as viewed
                markDocumentViewedUseCase(documentId)
            } catch (e: Exception) {
                _uiState.value = ViewerUiState.Error(e.message ?: "Failed to load document")
            }
        }
    }

    private fun resolveViewerType(mimeType: String): ViewerType {
        return when {
            mimeType == "application/pdf" -> ViewerType.PDF
            mimeType.startsWith("image/") -> ViewerType.IMAGE
            mimeType.startsWith("text/") -> ViewerType.TEXT
            else -> ViewerType.UNSUPPORTED
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(documentId)
            // Reload to update state
            val updated = documentRepository.getDocumentById(documentId) ?: return@launch
            val current = _uiState.value as? ViewerUiState.Ready ?: return@launch
            _uiState.value = current.copy(document = updated)
        }
    }

    fun showNotesDialog() { _showNotesDialog.value = true }
    fun hideNotesDialog() { _showNotesDialog.value = false }

    fun updateNotes(notes: String?) {
        viewModelScope.launch {
            updateDocumentNotesUseCase(documentId, notes)
            val updated = documentRepository.getDocumentById(documentId) ?: return@launch
            val current = _uiState.value as? ViewerUiState.Ready ?: return@launch
            _uiState.value = current.copy(document = updated)
            hideNotesDialog()
        }
    }

    fun setExpiryDate(date: Instant?) {
        viewModelScope.launch {
            setExpiryDateUseCase(documentId, date)
            val updated = documentRepository.getDocumentById(documentId) ?: return@launch
            val current = _uiState.value as? ViewerUiState.Ready ?: return@launch
            _uiState.value = current.copy(document = updated)
        }
    }
}
