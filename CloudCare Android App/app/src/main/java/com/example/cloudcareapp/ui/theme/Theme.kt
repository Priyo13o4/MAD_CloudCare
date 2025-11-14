package com.example.cloudcareapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,
    
    tertiary = Info,
    onTertiary = Color.White,
    
    error = Error,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = Color(0xFF8B0000),
    
    background = Background,
    onBackground = TextPrimary,
    
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = TextTertiary,
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF001D36),
    primaryContainer = Primary,
    onPrimaryContainer = Color.White,
    
    secondary = SecondaryLight,
    onSecondary = Color(0xFF2D1B4E),
    secondaryContainer = Secondary,
    onSecondaryContainer = Color.White,
    
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun CloudCareAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}