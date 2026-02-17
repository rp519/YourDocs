package com.yourdocs.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdocs.data.billing.BillingEvent
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.model.LockMethod
import com.yourdocs.ui.components.FolderColorPicker
import com.yourdocs.ui.components.GradientTopBar
import com.yourdocs.ui.components.LockMethodPickerDialog
import com.yourdocs.ui.components.PinEntryDialog
import com.yourdocs.ui.components.SetupPinDialog
import com.yourdocs.ui.components.UpgradeBottomSheet
import com.yourdocs.ui.theme.IvoryWhite
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val showRenameDialog by viewModel.showRenameDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val productDetails by viewModel.billingManager.productDetails.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Auth state
    var pendingAuthAction by remember { mutableStateOf<PendingAuthAction?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinDialogTitle by remember { mutableStateOf("Enter PIN") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showLockMethodPicker by remember { mutableStateOf<String?>(null) } // folderId
    var showSetupPin by remember { mutableStateOf<Pair<String, LockMethod>?>(null) } // folderId, method

    // Upgrade sheet state
    var showUpgradeSheet by remember { mutableStateOf(false) }
    var upgradeTriggerFeature by remember { mutableStateOf<String?>(null) }

    // Check biometric availability
    val biometricAvailable = remember {
        val bm = BiometricManager.from(context)
        bm.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun launchBiometric(action: PendingAuthAction, title: String, subtitle: String, allowPinFallback: Boolean) {
        val activity = context as? FragmentActivity ?: return
        val promptBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)

        if (allowPinFallback) {
            promptBuilder.setNegativeButtonText("Use PIN")
        } else {
            promptBuilder.setNegativeButtonText("Cancel")
        }

        val promptInfo = promptBuilder.build()
        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.onAuthSuccess(action)
                }
                override fun onAuthenticationFailed() {
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON && allowPinFallback) {
                        // User chose "Use PIN" — show PIN dialog
                        pendingAuthAction = action
                        pinError = null
                        showPinDialog = true
                    } else if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(context, errString.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        biometricPrompt.authenticate(promptInfo)
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToFolder -> {
                    onFolderClick(event.folderId)
                }
                is HomeEvent.RequestAuthentication -> {
                    when (event.lockMethod) {
                        LockMethod.BIOMETRIC -> {
                            if (biometricAvailable) {
                                launchBiometric(event.action, event.title, event.subtitle, allowPinFallback = false)
                            } else {
                                // Biometric not available, let through
                                viewModel.onAuthSuccess(event.action)
                            }
                        }
                        LockMethod.PIN -> {
                            pendingAuthAction = event.action
                            pinDialogTitle = event.title
                            pinError = null
                            showPinDialog = true
                        }
                        LockMethod.BOTH -> {
                            if (biometricAvailable) {
                                launchBiometric(event.action, event.title, event.subtitle, allowPinFallback = true)
                            } else {
                                // Fallback to PIN only
                                pendingAuthAction = event.action
                                pinDialogTitle = event.title
                                pinError = null
                                showPinDialog = true
                            }
                        }
                    }
                }
                is HomeEvent.ShowLockMethodPicker -> {
                    showLockMethodPicker = event.folderId
                }
                is HomeEvent.ShowSetupPin -> {
                    showSetupPin = Pair(event.folderId, event.method)
                }
                is HomeEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is HomeEvent.ShowUpgradeSheet -> {
                    upgradeTriggerFeature = event.triggerFeature
                    showUpgradeSheet = true
                }
            }
        }
    }

    // Handle billing events
    LaunchedEffect(Unit) {
        viewModel.billingEvents.collect { event ->
            when (event) {
                is BillingEvent.PurchaseSuccess -> {
                    showUpgradeSheet = false
                    Toast.makeText(context, "Welcome to YourDocs Pro!", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.PurchaseCancelled -> { /* No action needed */ }
            }
        }
    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "YourDocs",
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Folder") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HomeUiState.Empty -> {
                    EmptyState(
                        onCreateClick = { viewModel.showCreateDialog() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HomeUiState.Success -> {
                    FolderList(
                        folders = state.folders,
                        onFolderClick = { viewModel.onFolderClick(it) },
                        onRenameClick = { viewModel.showRenameDialog(it) },
                        onDeleteClick = { viewModel.showDeleteDialog(it) },
                        onPinClick = { viewModel.togglePinned(it.id) },
                        onLockClick = { viewModel.toggleLocked(it) }
                    )
                }

                is HomeUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onConfirm = { name, colorHex, description ->
                viewModel.createFolder(name, colorHex, description = description)
            },
            isPremium = isPremium
        )
    }

    showRenameDialog?.let { folder ->
        RenameFolderDialog(
            folder = folder,
            onDismiss = { viewModel.hideRenameDialog() },
            onConfirm = { name, description -> viewModel.renameFolder(folder.id, name, description) }
        )
    }

    showDeleteDialog?.let { folder ->
        DeleteFolderDialog(
            folder = folder,
            onDismiss = { viewModel.hideDeleteDialog() },
            onConfirm = { viewModel.deleteFolder(folder.id) }
        )
    }

    // PIN entry dialog
    if (showPinDialog) {
        PinEntryDialog(
            title = pinDialogTitle,
            errorMessage = pinError,
            onDismiss = {
                showPinDialog = false
                pendingAuthAction = null
                pinError = null
            },
            onConfirm = { pin ->
                coroutineScope.launch {
                    val valid = viewModel.verifyPin(pin)
                    if (valid) {
                        showPinDialog = false
                        pinError = null
                        pendingAuthAction?.let { viewModel.onAuthSuccess(it) }
                        pendingAuthAction = null
                    } else {
                        pinError = "Incorrect PIN"
                    }
                }
            }
        )
    }

    // Lock method picker dialog
    showLockMethodPicker?.let { folderId ->
        LockMethodPickerDialog(
            biometricAvailable = biometricAvailable,
            onDismiss = { showLockMethodPicker = null },
            onConfirm = { method ->
                showLockMethodPicker = null
                viewModel.onLockMethodChosen(folderId, method)
            }
        )
    }

    // Setup PIN dialog (when locking with PIN for the first time)
    showSetupPin?.let { (folderId, method) ->
        SetupPinDialog(
            onDismiss = { showSetupPin = null },
            onConfirm = { pin ->
                showSetupPin = null
                viewModel.onPinSetupComplete(folderId, method, pin)
            }
        )
    }

    // Upgrade bottom sheet
    if (showUpgradeSheet) {
        UpgradeBottomSheet(
            onDismiss = { showUpgradeSheet = false },
            onUpgradeClick = {
                val activity = context as? Activity ?: return@UpgradeBottomSheet
                viewModel.billingManager.launchPurchaseFlow(activity)
            },
            price = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice,
            triggerFeature = upgradeTriggerFeature,
            onTermsClick = onTermsOfServiceClick
        )
    }
}

@Composable
fun EmptyState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome to YourDocs",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your documents, organized your way",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onCreateClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First Folder")
        }
    }
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun FolderList(
    folders: List<Folder>,
    onFolderClick: (Folder) -> Unit,
    onRenameClick: (Folder) -> Unit,
    onDeleteClick: (Folder) -> Unit,
    onPinClick: (Folder) -> Unit,
    onLockClick: (Folder) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        items(folders, key = { it.id }) { folder ->
            FolderItem(
                folder = folder,
                onClick = { onFolderClick(folder) },
                onRenameClick = { onRenameClick(folder) },
                onDeleteClick = { onDeleteClick(folder) },
                onPinClick = { onPinClick(folder) },
                onLockClick = { onLockClick(folder) }
            )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun FolderItem(
    folder: Folder,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPinClick: () -> Unit,
    onLockClick: () -> Unit
) {
    val folderColor = folder.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) }
        catch (_: Exception) { MaterialTheme.colorScheme.primary }
    } ?: MaterialTheme.colorScheme.primary

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Emoji or folder icon inline with name
                    if (!folder.emoji.isNullOrEmpty()) {
                        Text(text = folder.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = if (folder.isLocked) Icons.Default.Lock else Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = folderColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (folder.isLocked && !folder.emoji.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (folder.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Description
                if (!folder.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = folder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Info badge row
                Row(
                    modifier = Modifier
                        .background(
                            IvoryWhite,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = buildString {
                            append("${folder.documentCount} ${if (folder.documentCount == 1) "doc" else "docs"}")
                            append(" \u00b7 ")
                            append(formatRelativeDate(folder.updatedAt))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.87f)
                    )
                }
            }

            // Three-dot overflow menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onRenameClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (folder.isPinned) "Unpin" else "Pin") },
                        onClick = {
                            onPinClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                tint = if (folder.isPinned) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (folder.isLocked) "Unlock" else "Lock") },
                        onClick = {
                            onLockClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (folder.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (folder.isLocked) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String?, description: String?) -> Unit,
    isPremium: Boolean = true
) {
    var folderName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isPremium) {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FolderColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Custom colors available with Pro",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        folderName,
                        if (isPremium) selectedColor else null,
                        description.ifBlank { null }
                    )
                },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var folderName by remember { mutableStateOf(folder.name) }
    var description by remember { mutableStateOf(folder.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder") },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(folderName, description.ifBlank { null })
                },
                enabled = folderName.isNotBlank() &&
                        (folderName != folder.name || description.ifBlank { null } != folder.description)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Delete Folder") },
        text = {
            Column {
                Text("Delete \"${folder.name}\" and all its documents?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatRelativeDate(instant: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(instant, now)
    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
        duration.toHours() < 24 -> "${duration.toHours()}h ago"
        duration.toDays() < 7 -> "${duration.toDays()} ${if (duration.toDays() == 1L) "day" else "days"} ago"
        duration.toDays() < 30 -> "${duration.toDays() / 7} ${if (duration.toDays() / 7 == 1L) "week" else "weeks"} ago"
        else -> instant.atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}
