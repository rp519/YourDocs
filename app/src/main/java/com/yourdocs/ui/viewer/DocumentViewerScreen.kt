package com.yourdocs.ui.viewer

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdocs.ui.components.EditNotesDialog
import com.yourdocs.ui.components.GradientTopBar

@Composable
fun DocumentViewerScreen(
    onNavigateBack: () -> Unit,
    viewModel: DocumentViewerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showNotesDialog by viewModel.showNotesDialog.collectAsState()

    val title = when (val state = uiState) {
        is ViewerUiState.Ready -> state.document.originalName
        else -> "Document"
    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    val readyState = uiState as? ViewerUiState.Ready
                    if (readyState != null) {
                        // Favorite toggle
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (readyState.document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (readyState.document.isFavorite) Color(0xFFFFB300) else Color.White
                            )
                        }
                        // Notes
                        IconButton(onClick = { viewModel.showNotesDialog() }) {
                            Icon(Icons.Default.NoteAlt, contentDescription = "Notes", tint = Color.White)
                        }
                        // Share
                        IconButton(onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", readyState.file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = readyState.document.mimeType
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show()
                            }
                        }) { Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White) }
                        // Open externally
                        IconButton(onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", readyState.file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, readyState.document.mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
                            }
                        }) { Icon(Icons.Default.OpenInNew, contentDescription = "Open externally", tint = Color.White) }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is ViewerUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ViewerUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                is ViewerUiState.Ready -> {
                    when (state.viewerType) {
                        ViewerType.PDF -> PdfViewerComposable(file = state.file)
                        ViewerType.IMAGE -> ImageViewerComposable(file = state.file)
                        ViewerType.TEXT -> TextViewerComposable(file = state.file)
                        ViewerType.UNSUPPORTED -> {
                            UnsupportedFileView(
                                document = state.document,
                                onOpenExternally = {
                                    try {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", state.file)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, state.document.mimeType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }

    // Notes dialog
    if (showNotesDialog) {
        val doc = (uiState as? ViewerUiState.Ready)?.document
        EditNotesDialog(
            currentNotes = doc?.notes,
            onDismiss = { viewModel.hideNotesDialog() },
            onConfirm = { notes -> viewModel.updateNotes(notes) }
        )
    }
}

@Composable
private fun UnsupportedFileView(
    document: com.yourdocs.domain.model.Document,
    onOpenExternally: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = document.originalName, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Type: ${document.mimeType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Size: ${formatSize(document.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenExternally) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Open Externally")
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}
