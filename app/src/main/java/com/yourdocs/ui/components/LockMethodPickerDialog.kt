package com.yourdocs.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourdocs.domain.model.LockMethod

@Composable
fun LockMethodPickerDialog(
    biometricAvailable: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LockMethod) -> Unit
) {
    var selected by remember {
        mutableStateOf(if (biometricAvailable) LockMethod.BIOMETRIC else LockMethod.PIN)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Lock Method") },
        text = {
            Column {
                LockMethodOption(
                    label = "Biometric (Fingerprint)",
                    description = "Use fingerprint to unlock",
                    selected = selected == LockMethod.BIOMETRIC,
                    enabled = biometricAvailable,
                    onClick = { selected = LockMethod.BIOMETRIC }
                )
                Spacer(modifier = Modifier.height(4.dp))
                LockMethodOption(
                    label = "PIN",
                    description = "Use a 4-6 digit PIN to unlock",
                    selected = selected == LockMethod.PIN,
                    enabled = true,
                    onClick = { selected = LockMethod.PIN }
                )
                Spacer(modifier = Modifier.height(4.dp))
                LockMethodOption(
                    label = "Both",
                    description = "Biometric with PIN as fallback",
                    selected = selected == LockMethod.BOTH,
                    enabled = biometricAvailable,
                    onClick = { selected = LockMethod.BOTH }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("Lock")
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
private fun LockMethodOption(
    label: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = if (!enabled) "Not available on this device" else description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
    }
}
