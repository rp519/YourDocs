package com.yourdocs.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourdocs.domain.model.Folder

@Composable
fun FolderPickerDialog(
    folders: List<Folder>,
    currentFolderId: String,
    onDismiss: () -> Unit,
    onFolderSelected: (String) -> Unit
) {
    val filteredFolders = folders.filter { it.id != currentFolderId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to Folder") },
        text = {
            if (filteredFolders.isEmpty()) {
                Text("No other folders available")
            } else {
                LazyColumn {
                    items(filteredFolders) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFolderSelected(folder.id) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!folder.emoji.isNullOrEmpty()) {
                                Text(text = folder.emoji, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${folder.documentCount} docs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
