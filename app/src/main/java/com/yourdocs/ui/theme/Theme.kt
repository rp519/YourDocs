package com.yourdocs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Immutable
data class YourDocsColorScheme(
    val gradientStart: Color = GradientStart,
    val gradientEnd: Color = GradientEnd,
    val pdfColor: Color = PdfColor,
    val imageColor: Color = ImageColor,
    val textColor: Color = TextColor,
    val videoColor: Color = VideoColor,
    val audioColor: Color = AudioColor,
    val genericFileColor: Color = GenericFileColor,
)

val LocalYourDocsColors = staticCompositionLocalOf { YourDocsColorScheme() }

private val LightColorScheme = lightColorScheme(
    primary = Teal40,                        // Soft teal
    onPrimary = Color.White,
    primaryContainer = WarmTeal90,            // Warm cream container (no blue cast)
    onPrimaryContainer = Teal10,

    secondary = LeafGreen,                   // Logo tree green
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F0EB),  // Warm light green-gray (no green tint)
    onSecondaryContainer = Color(0xFF1B5E20),

    tertiary = AccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E6),   // Warm cream-peach
    onTertiaryContainer = Color(0xFF2B1700),

    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error10,

    background = Color(0xFFFFFCF9),          // Creamy white
    onBackground = WarmGray10,

    surface = Color(0xFFFFFEFB),             // Near-white with warm touch
    onSurface = WarmGray10,

    surfaceVariant = Color(0xFFF5F2EF),      // Warm light gray
    onSurfaceVariant = WarmGray50,

    outline = WarmGray80,
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal60,                        // Lighter teal for dark
    onPrimary = Teal10,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,

    secondary = Color(0xFF81C784),           // Light green
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = LeafGreenLight,

    tertiary = AccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF643F00),
    onTertiaryContainer = Color(0xFFFFDDB3),

    error = Error80,
    onError = Error10,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error90,

    background = Color(0xFF1A1918),          // Warm dark
    onBackground = Color(0xFFECE9E6),

    surface = Color(0xFF242220),             // Warm dark surface
    onSurface = Color(0xFFECE9E6),

    surfaceVariant = Color(0xFF2E2C2A),
    onSurfaceVariant = Color(0xFFB0ADAA),

    outline = Color(0xFF8A8785),
)

@Composable
fun YourDocsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val yourDocsColors = YourDocsColorScheme()

    CompositionLocalProvider(LocalYourDocsColors provides yourDocsColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
