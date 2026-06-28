package io.kshitij.logkeep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val LkPrimary = Color(0xFF534AB7)
internal val LkOnPrimary = Color.White
internal val LkPrimaryContainer = Color(0xFFEEEDFE)
internal val LkOnPrimaryContainer = Color(0xFF3C3489)
internal val LkBackground = Color(0xFFF6F5FB)
internal val LkOnBackground = Color(0xFF1A1025)
internal val LkSurface = Color.White
internal val LkOnSurface = Color(0xFF1A1025)
internal val LkSurfaceVariant = Color(0xFFECEAF4)
internal val LkOnSurfaceVariant = Color(0xFF666380)
internal val LkError = Color(0xFFE24B4A)
internal val LkOnError = Color.White
internal val LkOutline = Color(0xFFD5D3E0)
internal val LkOutlineVariant = Color(0xFFECEAF4)

private val LogKeepColorScheme = lightColorScheme(
    primary = LkPrimary,
    onPrimary = LkOnPrimary,
    primaryContainer = LkPrimaryContainer,
    onPrimaryContainer = LkOnPrimaryContainer,
    background = LkBackground,
    onBackground = LkOnBackground,
    surface = LkSurface,
    onSurface = LkOnSurface,
    surfaceVariant = LkSurfaceVariant,
    onSurfaceVariant = LkOnSurfaceVariant,
    error = LkError,
    onError = LkOnError,
    outline = LkOutline,
    outlineVariant = LkOutlineVariant
)

@Composable
internal fun LogKeepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LogKeepColorScheme,
        content = content
    )
}
