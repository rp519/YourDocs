package com.yourdocs.ui.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.local.storage.FileStorageManager
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
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
    private val fileStorageManager: FileStorageManager
) : ViewModel() {

    private val documentId: String = savedStateHandle.get<String>("documentId")!!

    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

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
}
