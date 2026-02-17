package com.yourdocs.ui.folder

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.yourdocs.data.billing.BillingEvent
import com.yourdocs.domain.model.Document
import com.yourdocs.domain.model.DocumentSource
import com.yourdocs.domain.model.SortPreference
import com.yourdocs.domain.model.Tag
import com.yourdocs.ui.components.EditNotesDialog
import com.yourdocs.ui.components.FolderPickerDialog
import com.yourdocs.ui.components.SearchableTopBar
import com.yourdocs.ui.components.TagChip
import com.yourdocs.ui.components.TagManagementDialog
import com.yourdocs.ui.components.UpgradeBottomSheet
import com.yourdocs.ui.theme.LocalYourDocsColors
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    onNavigateBack: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onTermsOfServiceClick: () -> Unit = {},
    viewModel: FolderDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val folderName by viewModel.folderName.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val showRenameDialog by viewModel.showRenameDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showNotesDialog by viewModel.showNotesDialog.collectAsState()
    val showExpiryDialog by viewModel.showExpiryDialog.collectAsState()
    val showTagDialog by viewModel.showTagDialog.collectAsState()
    val showMoveDialog by viewModel.showMoveDialog.collectAsState()
    val pendingDuplicate by viewModel.pendingDuplicate.collectAsState()
    val productDetails by viewModel.billingManager.productDetails.collectAsState()
    val sortPreference by viewModel.sortPreference.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val documentTags by viewModel.documentTags.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showImportMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showUpgradeSheet by remember { mutableStateOf(false) }
    var upgradeTriggerFeature by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> if (uris.isNotEmpty()) viewModel.onMultipleFilesSelected(uris, DocumentSource.IMPORT) }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> if (uris.isNotEmpty()) viewModel.onMultipleFilesSelected(uris, DocumentSource.GALLERY) }

    val activity = context as? Activity
    val scannerClient = remember {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(20)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .build()
        GmsDocumentScanning.getClient(options)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            GmsDocumentScanningResult.fromActivityResultIntent(result.data)?.pdf?.let { pdf ->
                viewModel.onScanCompleted(pdf.uri, pdf.pageCount)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FolderDetailEvent.LaunchFilePicker -> filePickerLauncher.launch(arrayOf("*/*"))
                is FolderDetailEvent.LaunchGalleryPicker -> galleryPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
                is FolderDetailEvent.NavigateToViewer -> onDocumentClick(event.documentId)
                is FolderDetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is FolderDetailEvent.ShowUpgradeSheet -> {
                    upgradeTriggerFeature = event.triggerFeature
                    showUpgradeSheet = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.billingEvents.collect { event ->
            when (event) {
                is BillingEvent.PurchaseSuccess -> {
                    showUpgradeSheet = false
                    Toast.makeText(context, "Welcome to YourDocs Pro!", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is BillingEvent.PurchaseCancelled -> {}
            }
        }
    }

    BackHandler(enabled = isSearchActive || isSelectionMode) {
        if (isSelectionMode) viewModel.exitSelectionMode()
        else viewModel.onSearchActiveChange(false)
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedIds.size,
                    onClose = { viewModel.exitSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onDelete = { viewModel.deleteSelected() },
                    onMove = { viewModel.showMoveDialog() }
                )
            } else {
                SearchableTopBar(
                    title = folderName,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { viewModel.onSearchActiveChange(it) },
                    searchPlaceholder = "Search documents...",
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isSearchActive) viewModel.onSearchActiveChange(false)
                            else onNavigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        // Sort button
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = Color.White)
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                SortPreference.entries.forEach { pref ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(sortLabel(pref))
                                                if (sortPreference == pref) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("✓", color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSortPreference(pref)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                Box {
                    FloatingActionButton(
                        onClick = { showImportMenu = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) { Icon(Icons.Default.Add, contentDescription = "Import Document") }

                    DropdownMenu(expanded = showImportMenu, onDismissRequest = { showImportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Import from Files") },
                            onClick = { showImportMenu = false; viewModel.onImportFromFiles() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Import from Gallery") },
                            onClick = { showImportMenu = false; viewModel.onImportFromGallery() },
                            leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Capture Document") },
                            onClick = {
                                showImportMenu = false
                                if (activity != null) {
                                    scannerClient.getStartScanIntent(activity)
                                        .addOnSuccessListener { intentSender ->
                                            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Scanner unavailable. Check Google Play Services.", Toast.LENGTH_LONG).show()
                                        }
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is FolderDetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is FolderDetailUiState.Empty -> DocumentEmptyState(modifier = Modifier.align(Alignment.Center))
                is FolderDetailUiState.Success -> {
                    if (state.documents.isEmpty() && isSearchActive) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No documents match \"$searchQuery\"", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        DocumentList(
                            documents = state.documents,
                            isSelectionMode = isSelectionMode,
                            selectedIds = selectedIds,
                            documentTags = documentTags,
                            onDocumentClick = { doc ->
                                if (isSelectionMode) viewModel.toggleSelection(doc.id)
                                else viewModel.onDocumentClick(doc)
                            },
                            onLongPress = { doc -> viewModel.enterSelectionMode(doc.id) },
                            onRenameClick = { viewModel.showRenameDialog(it) },
                            onDeleteClick = { viewModel.showDeleteDialog(it) },
                            onFavoriteClick = { viewModel.toggleFavorite(it.id) },
                            onNotesClick = { viewModel.showNotesDialog(it) },
                            onExpiryClick = { viewModel.showExpiryDialog(it) },
                            onTagsClick = { viewModel.showTagDialog(it) },
                            onShareClick = { doc ->
                                try {
                                    val file = File(context.filesDir, "YourDocs/documents/${doc.storedFileName}")
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = doc.mimeType
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
                is FolderDetailUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (isImporting) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Card {
                            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text(importProgress)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    showRenameDialog?.let { document ->
        RenameDocumentDialog(document = document, onDismiss = { viewModel.hideRenameDialog() },
            onConfirm = { newName -> viewModel.renameDocument(document.id, newName) })
    }

    showDeleteDialog?.let { document ->
        DeleteDocumentDialog(document = document, onDismiss = { viewModel.hideDeleteDialog() },
            onConfirm = { viewModel.deleteDocument(document.id) })
    }

    showNotesDialog?.let { document ->
        EditNotesDialog(currentNotes = document.notes, onDismiss = { viewModel.hideNotesDialog() },
            onConfirm = { notes -> viewModel.updateDocumentNotes(document.id, notes) })
    }

    showExpiryDialog?.let { document ->
        ExpiryDatePickerDialog(
            currentExpiry = document.expiryDate,
            onDismiss = { viewModel.hideExpiryDialog() },
            onConfirm = { date -> viewModel.setExpiryDate(document.id, date) }
        )
    }

    showTagDialog?.let { document ->
        TagManagementDialog(
            documentTags = documentTags[document.id] ?: emptyList(),
            allTags = allTags,
            onDismiss = { viewModel.hideTagDialog() },
            onAddTag = { name, color -> viewModel.createAndAssignTag(document.id, name, color) },
            onRemoveTag = { tagId -> viewModel.removeTagFromDocument(document.id, tagId) },
            onAssignTag = { tagId -> viewModel.assignTagToDocument(document.id, tagId) }
        )
    }

    if (showMoveDialog) {
        FolderPickerDialog(
            folders = allFolders,
            currentFolderId = viewModel.folderId,
            onDismiss = { viewModel.hideMoveDialog() },
            onFolderSelected = { viewModel.moveSelected(it) }
        )
    }

    pendingDuplicate?.let { dup ->
        DuplicateDocumentDialog(
            documentName = dup.metadata.displayName,
            onReplace = { viewModel.onDuplicateReplace(dup.existingDocument.id, dup.uri, dup.source) },
            onKeepBoth = { viewModel.onDuplicateKeepBoth(dup.uri, dup.source, dup.metadata.displayName) },
            onCancel = { viewModel.onDuplicateCancel() }
        )
    }

    if (showUpgradeSheet) {
        UpgradeBottomSheet(
            onDismiss = { showUpgradeSheet = false },
            onUpgradeClick = { (context as? Activity)?.let { viewModel.billingManager.launchPurchaseFlow(it) } },
            price = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice,
            triggerFeature = upgradeTriggerFeature,
            onTermsClick = onTermsOfServiceClick
        )
    }
}

@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    val colors = LocalYourDocsColors.current
    val gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(colors.gradientStart, colors.gradientEnd)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(top = 40.dp)
            .height(64.dp)
            .padding(horizontal = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White) }
            Text("$selectedCount selected", style = MaterialTheme.typography.titleLarge, color = Color.White,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            IconButton(onClick = onSelectAll) { Icon(Icons.Default.SelectAll, contentDescription = "Select all", tint = Color.White) }
            IconButton(onClick = onMove) { Icon(Icons.Default.DriveFileMove, contentDescription = "Move", tint = Color.White) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpiryDatePickerDialog(
    currentExpiry: Instant?,
    onDismiss: () -> Unit,
    onConfirm: (Instant?) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = currentExpiry?.toEpochMilli() ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(Instant.ofEpochMilli(it)) }
            }) { Text("Set") }
        },
        dismissButton = {
            Row {
                if (currentExpiry != null) {
                    TextButton(onClick = { onConfirm(null) }) { Text("Clear") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun DuplicateDocumentDialog(
    documentName: String,
    onReplace: () -> Unit,
    onKeepBoth: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Duplicate Document") },
        text = { Text("A file named \"$documentName\" with the same size already exists in this folder.") },
        confirmButton = {
            TextButton(onClick = onReplace) { Text("Replace") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onKeepBoth) { Text("Keep Both") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun DocumentEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("No documents yet", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Import files, photos, or scan documents", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DocumentList(
    documents: List<Document>,
    isSelectionMode: Boolean,
    selectedIds: Set<String>,
    documentTags: Map<String, List<Tag>>,
    onDocumentClick: (Document) -> Unit,
    onLongPress: (Document) -> Unit,
    onRenameClick: (Document) -> Unit,
    onDeleteClick: (Document) -> Unit,
    onFavoriteClick: (Document) -> Unit,
    onNotesClick: (Document) -> Unit,
    onExpiryClick: (Document) -> Unit,
    onTagsClick: (Document) -> Unit,
    onShareClick: (Document) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(documents, key = { it.id }) { document ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(document.id) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                modifier = Modifier.animateItem()
            ) {
                DocumentItem(
                    document = document,
                    isSelected = document.id in selectedIds,
                    isSelectionMode = isSelectionMode,
                    tags = documentTags[document.id] ?: emptyList(),
                    onClick = { onDocumentClick(document) },
                    onLongPress = { onLongPress(document) },
                    onRenameClick = { onRenameClick(document) },
                    onDeleteClick = { onDeleteClick(document) },
                    onFavoriteClick = { onFavoriteClick(document) },
                    onNotesClick = { onNotesClick(document) },
                    onExpiryClick = { onExpiryClick(document) },
                    onTagsClick = { onTagsClick(document) },
                    onShareClick = { onShareClick(document) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DocumentItem(
    document: Document,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    tags: List<Tag>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onNotesClick: () -> Unit,
    onExpiryClick: () -> Unit,
    onTagsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val yourDocsColors = LocalYourDocsColors.current
    val isImage = document.mimeType.startsWith("image/")

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox
            if (isSelectionMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Thumbnail or icon
            if (isImage) {
                val context = LocalContext.current
                val file = remember(document.storedFileName) {
                    File(context.filesDir, "YourDocs/documents/${document.storedFileName}")
                }
                AsyncImage(model = file, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop)
            } else {
                val (icon, tint) = getFileTypeIconAndColor(document.mimeType, yourDocsColors)
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = tint)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(document.originalName, style = MaterialTheme.typography.titleSmall, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (document.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Star, contentDescription = "Favorite", modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFB300))
                    }
                }

                // Notes preview
                if (!document.notes.isNullOrEmpty()) {
                    Text(document.notes, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val typeLabel = getDocTypeLabel(document.mimeType)
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(typeLabel, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text(formatFileSize(document.sizeBytes), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\u2022", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDocumentDate(document.createdAt), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Expiry badge
                    document.expiryDate?.let { expiry ->
                        val daysUntil = ChronoUnit.DAYS.between(Instant.now(), expiry)
                        val badgeColor = when {
                            daysUntil <= 0 -> Color(0xFFE53935)
                            daysUntil <= 7 -> Color(0xFFFF8F00)
                            else -> Color(0xFF43A047)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = badgeColor.copy(alpha = 0.15f)) {
                            Text(
                                text = when {
                                    daysUntil <= 0 -> "Expired"
                                    daysUntil == 1L -> "1 day"
                                    else -> "${daysUntil}d"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Tags
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        tags.take(3).forEach { tag ->
                            TagChip(name = tag.name, colorHex = tag.colorHex)
                        }
                        if (tags.size > 3) {
                            Text("+${tags.size - 3}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            if (!isSelectionMode) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (document.isFavorite) "Unfavorite" else "Favorite") },
                            onClick = { onFavoriteClick(); showMenu = false },
                            leadingIcon = { Icon(if (document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null,
                                tint = if (document.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Note") },
                            onClick = { onNotesClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Tags") },
                            onClick = { onTagsClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Expiry") },
                            onClick = { onExpiryClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = { onShareClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = { onRenameClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDeleteClick(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameDocumentDialog(document: Document, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var documentName by remember { mutableStateOf(document.originalName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Document") },
        text = { OutlinedTextField(value = documentName, onValueChange = { documentName = it }, label = { Text("Document Name") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(documentName) }, enabled = documentName.isNotBlank() && documentName != document.originalName) { Text("Rename") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeleteDocumentDialog(document: Document, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Delete Document") },
        text = { Column { Text("Delete \"${document.originalName}\"?"); Spacer(Modifier.height(8.dp)); Text("This action cannot be undone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) } },
        confirmButton = { TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun sortLabel(pref: SortPreference): String = when (pref) {
    SortPreference.NAME_ASC -> "Name A→Z"
    SortPreference.NAME_DESC -> "Name Z→A"
    SortPreference.DATE_NEWEST -> "Newest First"
    SortPreference.DATE_OLDEST -> "Oldest First"
    SortPreference.SIZE_LARGEST -> "Largest First"
    SortPreference.SIZE_SMALLEST -> "Smallest First"
    SortPreference.TYPE -> "File Type"
}

private fun getFileTypeIconAndColor(mimeType: String, colors: com.yourdocs.ui.theme.YourDocsColorScheme): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when {
        mimeType == "application/pdf" -> Icons.Default.PictureAsPdf to colors.pdfColor
        mimeType.startsWith("image/") -> Icons.Default.Image to colors.imageColor
        mimeType.startsWith("text/") -> Icons.Default.Description to colors.textColor
        mimeType.startsWith("video/") -> Icons.Default.Videocam to colors.videoColor
        mimeType.startsWith("audio/") -> Icons.Default.AudioFile to colors.audioColor
        else -> Icons.AutoMirrored.Filled.InsertDriveFile to colors.genericFileColor
    }
}

private fun getDocTypeLabel(mimeType: String): String = when {
    mimeType == "application/pdf" -> "PDF"
    mimeType.startsWith("image/") -> "Image"
    mimeType.startsWith("text/") -> "Text"
    mimeType.startsWith("video/") -> "Video"
    mimeType.startsWith("audio/") -> "Audio"
    else -> "File"
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private fun formatDocumentDate(instant: Instant): String = instant.atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
