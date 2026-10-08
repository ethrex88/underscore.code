package com.fitnix.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Palette(
    val dark: Boolean,
    val bg: Color,
    val card: Color,
    val cardAlt: Color,
    val stroke: Color,
    val text: Color,
    val textDim: Color,
    val primary: Color,
    val primaryDark: Color,
    val onPrimary: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val success: Color,
    val water: Color,
)

val DarkPalette = Palette(
    dark = true,
    bg = Color(0xFF0B0F0D),
    card = Color(0xFF141D18),
    cardAlt = Color(0xFF1C2A22),
    stroke = Color(0xFF233329),
    text = Color(0xFFFFFFFF),
    textDim = Color(0xFFA9B8AD),
    primary = Color(0xFF5DE8A5),
    primaryDark = Color(0xFF0BB781),
    onPrimary = Color(0xFF04140B),
    warning = Color(0xFFF3C87A),
    error = Color(0xFFFF6B6B),
    info = Color(0xFF9DBEFF),
    success = Color(0xFF34D399),
    water = Color(0xFF4F9DFF),
)

val LightPalette = Palette(
    dark = false,
    bg = Color(0xFFF2F6F4),
    card = Color(0xFFFFFFFF),
    cardAlt = Color(0xFFE7F0EB),
    stroke = Color(0xFFDAE5DF),
    text = Color(0xFF0E1A14),
    textDim = Color(0xFF5B6B62),
    primary = Color(0xFF10B474),
    primaryDark = Color(0xFF0A8F5F),
    onPrimary = Color(0xFFFFFFFF),
    warning = Color(0xFFD4931F),
    error = Color(0xFFE5484D),
    info = Color(0xFF3E7BFA),
    success = Color(0xFF16A34A),
    water = Color(0xFF2F80ED),
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

@Composable
fun FitnixTheme(dark: Boolean, content: @Composable () -> Unit) {
    val p = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.primary, onPrimary = p.onPrimary, background = p.bg, onBackground = p.text,
            surface = p.card, onSurface = p.text, surfaceVariant = p.cardAlt, onSurfaceVariant = p.textDim,
            surfaceContainer = p.card, surfaceContainerHigh = p.card, surfaceContainerHighest = p.cardAlt,
            outline = p.stroke, error = p.error,
        )
    } else {
        lightColorScheme(
            primary = p.primary, onPrimary = p.onPrimary, background = p.bg, onBackground = p.text,
            surface = p.card, onSurface = p.text, surfaceVariant = p.cardAlt, onSurfaceVariant = p.textDim,
            surfaceContainer = p.card, surfaceContainerHigh = p.card, surfaceContainerHighest = p.cardAlt,
            outline = p.stroke, error = p.error,
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
