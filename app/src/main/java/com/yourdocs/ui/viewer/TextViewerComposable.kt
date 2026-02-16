package com.yourdocs.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_TEXT_SIZE = 500 * 1024 // 500KB

@Composable
fun TextViewerComposable(
    file: File,
    modifier: Modifier = Modifier
) {
    var textContent by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                val size = file.length()
                textContent = if (size > MAX_TEXT_SIZE) {
                    file.inputStream().bufferedReader().use { reader ->
                        val buffer = CharArray(MAX_TEXT_SIZE)
                        val read = reader.read(buffer)
                        String(buffer, 0, read) + "\n\n--- File truncated (${size / 1024}KB total) ---"
                    }
                } else {
                    file.readText()
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to read file"
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            error != null -> {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            textContent == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                SelectionContainer {
                    Text(
                        text = textContent!!,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
