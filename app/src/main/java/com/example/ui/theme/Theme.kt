package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Slate950,
    primaryContainer = BrandPrimaryDark,
    onPrimaryContainer = Slate50,
    secondary = BrandSecondaryLight,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,
    tertiary = SuccessEmerald,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate50,
    surface = SurfaceDark,
    onSurface = Slate100,
    surfaceVariant = CardDark,
    onSurfaceVariant = Slate300,
    outline = CardBorderDark,
    error = DangerRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = SuccessEmeraldDark,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Slate900,
    surface = CardLight,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate700,
    outline = CardBorderLight,
    error = DangerRose,
    onError = Color.White
)

@Composable
fun BizFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false by default for strict SaaS brand consistency
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
