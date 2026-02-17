package com.yourdocs.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yourdocs.domain.model.DocumentWithFolderInfo
import com.yourdocs.ui.theme.LocalYourDocsColors
import java.io.File

@Composable
fun DocumentCardSmall(
    item: DocumentWithFolderInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doc = item.document
    val isImage = doc.mimeType.startsWith("image/")
    val context = LocalContext.current
    val yourDocsColors = LocalYourDocsColors.current

    Card(
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isImage) {
                val file = remember(doc.storedFileName) {
                    File(context.filesDir, "YourDocs/documents/${doc.storedFileName}")
                }
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val (icon, tint) = getSmallCardIcon(doc.mimeType, yourDocsColors)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = tint
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = doc.originalName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.folderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getSmallCardIcon(
    mimeType: String,
    colors: com.yourdocs.ui.theme.YourDocsColorScheme
): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when {
        mimeType == "application/pdf" -> Icons.Default.PictureAsPdf to colors.pdfColor
        mimeType.startsWith("image/") -> Icons.Default.Image to colors.imageColor
        mimeType.startsWith("text/") -> Icons.Default.Description to colors.textColor
        mimeType.startsWith("video/") -> Icons.Default.Videocam to colors.videoColor
        mimeType.startsWith("audio/") -> Icons.Default.AudioFile to colors.audioColor
        else -> Icons.AutoMirrored.Filled.InsertDriveFile to colors.genericFileColor
    }
}
