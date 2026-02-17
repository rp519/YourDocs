package com.yourdocs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TagChip(
    name: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val bgColor = if (selected) chipColor.copy(alpha = 0.3f) else chipColor.copy(alpha = 0.15f)
    val textColor = chipColor.copy(alpha = 1f)

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
        if (onRemove != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
                    .clickable { onRemove() },
                tint = textColor
            )
        }
    }
}
