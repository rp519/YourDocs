package com.yourdocs.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourdocs.ui.theme.LocalYourDocsColors

@Composable
fun SearchableTopBar(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    searchPlaceholder: String = "Search..."
) {
    val colors = LocalYourDocsColors.current
    val gradient = Brush.horizontalGradient(
        colors = listOf(colors.gradientStart, colors.gradientEnd)
    )
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(64.dp)
            .padding(horizontal = 4.dp),
    ) {
        AnimatedContent(
            targetState = isSearchActive,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "searchToggle",
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)
        ) { searching ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (searching) {
                    if (navigationIcon != null) {
                        navigationIcon()
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = searchPlaceholder,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize
                            ),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }

                    IconButton(onClick = {
                        onSearchQueryChange("")
                        onSearchActiveChange(false)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    if (navigationIcon != null) {
                        navigationIcon()
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )

                    IconButton(onClick = { onSearchActiveChange(true) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }

                    Row(content = actions)
                }
            }
        }
    }
}
