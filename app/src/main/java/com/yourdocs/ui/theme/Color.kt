package com.yourdocs.ui.theme

import androidx.compose.ui.graphics.Color

// Warm Cream & Soft Teal Color System — matching YD logo palette

// Primary - Soft Teal (from logo "YD" letters)
val Teal10 = Color(0xFF002020)
val Teal20 = Color(0xFF003737)
val Teal30 = Color(0xFF005050)
val Teal40 = Color(0xFF00796B)   // Primary teal
val Teal50 = Color(0xFF009688)
val Teal60 = Color(0xFF4DB6AC)
val Teal70 = Color(0xFF80CBC4)
val Teal80 = Color(0xFFB2DFDB)
val Teal90 = Color(0xFFE0F2F1)
val Teal95 = Color(0xFFF0FAF9)

// Warm Neutrals — cream / ivory base
val Cream = Color(0xFFFAF8F5)             // Main background (warm off-white)
val Ivory = Color(0xFFFFFFF0)             // Badge backgrounds
val WarmWhite = Color(0xFFFFFEFC)         // Card surfaces
val WarmGray10 = Color(0xFF1C1B1A)        // Darkest text
val WarmGray20 = Color(0xFF333130)        // Secondary dark text
val WarmGray50 = Color(0xFF7A7574)        // Muted text
val WarmGray80 = Color(0xFFD4CFCC)        // Borders
val WarmGray90 = Color(0xFFECE9E6)        // Dividers / subtle backgrounds
val WarmGray95 = Color(0xFFF5F3F0)        // Surface variant

// Accent - Soft Green (from logo tree)
val LeafGreen = Color(0xFF4CAF50)         // Logo tree green
val LeafGreenLight = Color(0xFFE8F5E9)

// Accent - Warm Amber
val AccentAmber = Color(0xFFFFA726)

// Error
val Error10 = Color(0xFF410002)
val Error40 = Color(0xFFD32F2F)
val Error80 = Color(0xFFFFB4AB)
val Error90 = Color(0xFFFFDAD6)

// Gradient colors for top bar — soft teal matching logo
val GradientStart = Color(0xFF00796B)     // Teal
val GradientEnd = Color(0xFF4DB6AC)       // Lighter teal

// Document type color coding
val PdfColor = Color(0xFFD32F2F)          // Red
val ImageColor = Color(0xFF388E3C)         // Green
val TextColor = Color(0xFF1976D2)          // Blue
val VideoColor = Color(0xFF7B1FA2)         // Purple
val AudioColor = Color(0xFFFF8F00)         // Orange
val GenericFileColor = Color(0xFF757575)   // Grey

// Folder color presets
val FolderColorPresets = listOf(
    "#00796B", // Teal (default — matches logo)
    "#D32F2F", // Red
    "#388E3C", // Green
    "#1976D2", // Blue
    "#7B1FA2", // Purple
    "#FF8F00", // Orange
    "#5D4037", // Brown
    "#455A64", // Blue Grey
)

// Ivory White (for detail badges)
val IvoryWhite = Color(0xFFFFFFF0)
