package com.core.app.supercalchub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 主题颜色配置
data class ThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color
)

// 预定义主题
object AppThemes {
    val Blue = ThemeColors(
        primary = Color(0xFF1976D2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF64B5F6),
        secondaryContainer = Color(0xFFE3F2FD),
        onSecondaryContainer = Color(0xFF1565C0)
    )
    val Green = ThemeColors(
        primary = Color(0xFF388E3C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF81C784),
        secondaryContainer = Color(0xFFE8F5E9),
        onSecondaryContainer = Color(0xFF2E7D32)
    )
    val Red = ThemeColors(
        primary = Color(0xFFD32F2F),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFCDD2),
        onPrimaryContainer = Color(0xFFB71C1C),
        secondary = Color(0xFFE57373),
        secondaryContainer = Color(0xFFFFEBEE),
        onSecondaryContainer = Color(0xFFC62828)
    )
    val Purple = ThemeColors(
        primary = Color(0xFF7B1FA2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE1BEE7),
        onPrimaryContainer = Color(0xFF4A148C),
        secondary = Color(0xFFBA68C8),
        secondaryContainer = Color(0xFFF3E5F5),
        onSecondaryContainer = Color(0xFF6A1B9A)
    )
    val Orange = ThemeColors(
        primary = Color(0xFFF57C00),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE0B2),
        onPrimaryContainer = Color(0xFFE65100),
        secondary = Color(0xFFFFB74D),
        secondaryContainer = Color(0xFFFFF3E0),
        onSecondaryContainer = Color(0xFFEF6C00)
    )
    val Teal = ThemeColors(
        primary = Color(0xFF00897B),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFB2DFDB),
        onPrimaryContainer = Color(0xFF004D40),
        secondary = Color(0xFF4DB6AC),
        secondaryContainer = Color(0xFFE0F2F1),
        onSecondaryContainer = Color(0xFF00695C)
    )
    
    fun getTheme(themeId: String): ThemeColors {
        return when (themeId) {
            "green" -> Green
            "red" -> Red
            "purple" -> Purple
            "orange" -> Orange
            "teal" -> Teal
            else -> Blue
        }
    }
}

@Composable
fun SuperCalcHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeId: String = "blue",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeColors = AppThemes.getTheme(themeId)
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = themeColors.primary,
            onPrimary = themeColors.onPrimary,
            primaryContainer = themeColors.primaryContainer,
            onPrimaryContainer = themeColors.onPrimaryContainer,
            secondary = themeColors.secondary,
            secondaryContainer = themeColors.secondaryContainer,
            onSecondaryContainer = themeColors.onSecondaryContainer,
            tertiary = themeColors.secondary
        )
        else -> lightColorScheme(
            primary = themeColors.primary,
            onPrimary = themeColors.onPrimary,
            primaryContainer = themeColors.primaryContainer,
            onPrimaryContainer = themeColors.onPrimaryContainer,
            secondary = themeColors.secondary,
            secondaryContainer = themeColors.secondaryContainer,
            onSecondaryContainer = themeColors.onSecondaryContainer,
            tertiary = themeColors.secondary
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}