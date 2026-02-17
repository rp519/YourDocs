package com.yourdocs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourdocs.domain.model.Tag

private val tagColorOptions = listOf(
    "#E53935", "#D81B60", "#8E24AA", "#5E35B1",
    "#3949AB", "#1E88E5", "#00897B", "#43A047",
    "#F4511E", "#6D4C41", "#546E7A", "#FFB300"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagManagementDialog(
    documentTags: List<Tag>,
    allTags: List<Tag>,
    onDismiss: () -> Unit,
    onAddTag: (String, String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onAssignTag: (String) -> Unit
) {
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(tagColorOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Tags") },
        text = {
            Column {
                // Current tags
                if (documentTags.isNotEmpty()) {
                    Text("Document tags:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        documentTags.forEach { tag ->
                            TagChip(
                                name = tag.name,
                                colorHex = tag.colorHex,
                                onRemove = { onRemoveTag(tag.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Available tags to add
                val availableTags = allTags.filter { at -> documentTags.none { dt -> dt.id == at.id } }
                if (availableTags.isNotEmpty()) {
                    Text("Available tags:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTags.forEach { tag ->
                            TagChip(
                                name = tag.name,
                                colorHex = tag.colorHex,
                                onClick = { onAssignTag(tag.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Create new tag
                Text("Create new tag:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { if (it.length <= 30) newTagName = it },
                        label = { Text("Tag name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTagName.isNotBlank()) {
                                onAddTag(newTagName.trim(), selectedColor)
                                newTagName = ""
                            }
                        },
                        enabled = newTagName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create tag")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tagColorOptions) { color ->
                        val parsed = try { Color(android.graphics.Color.parseColor(color)) } catch (_: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .clickable { selectedColor = color }
                                .then(
                                    if (color == selectedColor) Modifier.padding(2.dp)
                                        .background(Color.White, CircleShape)
                                        .padding(2.dp)
                                        .background(parsed, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
