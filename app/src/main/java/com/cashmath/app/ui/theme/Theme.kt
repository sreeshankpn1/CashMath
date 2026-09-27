package com.cashmath.app.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalThemeIsDark = staticCompositionLocalOf { false }

// Light Scheme (Apple / Modern Fintech Clean)
val LuxuryLightScheme = lightColorScheme(
    primary = Color(0xFF0F2646),          // Royal Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF), // Soft Ice-Indigo
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF059669),        // Vibrant Emerald (Cash In)
    onSecondary = Color.White,
    error = Color(0xFFDC2626),            // Cardinal Red (Cash Out / Due)
    onError = Color.White,
    background = Color(0xFFF8FAFC),       // Clean Crisp Slate
    onBackground = Color(0xFF0F172A),
    surface = Color.White,                // Pure White Card
    onSurface = Color(0xFF0F172A),         // High-Contrast Slate Text
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF64748B),  // Refined Muted Text
    outline = Color(0xFFE2E8F0)           // Razor-thin Card Border
)

// Dark Scheme (CRED / Obsidian Luxury)
val LuxuryDarkScheme = darkColorScheme(
    primary = Color(0xFF6366F1),          // Electric Indigo Accent
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B), // Elevated Dark Card
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFF00E676),        // Electric Emerald (Cash In)
    onSecondary = Color(0xFF064E3B),
    error = Color(0xFFEF4444),            // Coral Red (Cash Out)
    onError = Color(0xFF450A0A),
    background = Color(0xFF0A0E17),       // Deep Midnight Obsidian
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131B2E),          // Elevated Titanium Slate Surface
    onSurface = Color(0xFFF8FAFC),         // Crisp Off-White Text
    surfaceVariant = Color(0xFF1E293B),    // Elevated Inner Surface
    onSurfaceVariant = Color(0xFF94A3B8),  // Soft Slate Labels
    outline = Color(0xFF1E293B)           // Dark Slate Stroke
)

@Composable
fun CashMathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("cashmath_settings_prefs", Context.MODE_PRIVATE) }
    val themePref = prefs.getString("theme_preference", "SYSTEM") ?: "SYSTEM"

    val isDark = when (themePref) {
        "LIGHT" -> false  // STRICTLY forces 100% Light mode everywhere
        "DARK" -> true    // STRICTLY forces 100% Dark mode everywhere
        else -> darkTheme
    }

    val colorScheme = if (isDark) LuxuryDarkScheme else LuxuryLightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            val bgArgb = (if (isDark) Color(0xFF0A0E17) else Color.White).toArgb()
            @Suppress("DEPRECATION")
            window.statusBarColor = bgArgb
            @Suppress("DEPRECATION")
            window.navigationBarColor = bgArgb
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalThemeIsDark provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
