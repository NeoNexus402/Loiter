/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score

val DefaultThemeColor = Color(0xFFED5564)

/**
 * Creates an explicitly neutral dark color scheme with no chromatic content.
 * Used for non-Loiter layout themes to provide a grayish-black/dark background
 * while [LayoutThemeConfig.accentColor] provides vibrancy on specific UI elements.
 */
fun neutralDarkColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFFB0B0B0),
        onPrimary = Color(0xFF212121),
        primaryContainer = Color(0xFF2E2E2E),
        onPrimaryContainer = Color(0xFFD0D0D0),
        inversePrimary = Color(0xFF3A3A3A),
        secondary = Color(0xFF9E9E9E),
        onSecondary = Color(0xFF212121),
        secondaryContainer = Color(0xFF2E2E2E),
        onSecondaryContainer = Color(0xFFD0D0D0),
        tertiary = Color(0xFF9E9E9E),
        onTertiary = Color(0xFF212121),
        tertiaryContainer = Color(0xFF2E2E2E),
        onTertiaryContainer = Color(0xFFD0D0D0),
        error = Color(0xFFCF6679),
        onError = Color(0xFF212121),
        errorContainer = Color(0xFF3A2A2A),
        onErrorContainer = Color(0xFFF0D0D0),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE0E0E0),
        surface = Color(0xFF1A1A1A),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = Color(0xFFB0B0B0),
        outline = Color(0xFF3A3A3A),
        outlineVariant = Color(0xFF2A2A2A),
        inverseSurface = Color(0xFFE0E0E0),
        inverseOnSurface = Color(0xFF212121),
        surfaceContainerLowest = Color(0xFF121212),
        surfaceContainerLow = Color(0xFF161616),
        surfaceContainer = Color(0xFF1E1E1E),
        surfaceContainerHigh = Color(0xFF222222),
        surfaceContainerHighest = Color(0xFF282828),
        surfaceTint = Color(0xFFB0B0B0),
        scrim = Color.Black,
    )
}

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    forceBlackBackground: Boolean = false,
    useNeutralScheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // For non-Loiter themes, use an explicitly neutral color scheme
    if (useNeutralScheme && darkTheme) {
        val colorScheme = remember(pureBlack, forceBlackBackground) {
            if (pureBlack) {
                neutralDarkColorScheme().let { scheme ->
                    scheme.copy(
                        surface = Color.Black,
                        background = Color.Black,
                    )
                }
            } else if (forceBlackBackground) {
                neutralDarkColorScheme().let { scheme ->
                    scheme.copy(
                        surface = Color.Black,
                        background = Color.Black,
                        surfaceVariant = Color(0xFF1A1A1A),
                        surfaceContainer = Color(0xFF1A1A1A),
                        surfaceContainerHigh = Color(0xFF1A1A1A),
                        surfaceContainerHighest = Color(0xFF1A1A1A),
                        surfaceContainerLow = Color(0xFF1A1A1A),
                        surfaceContainerLowest = Color.Black,
                        outline = Color(0xFF2A2A2A),
                        outlineVariant = Color(0xFF2A2A2A),
                    )
                }
            } else {
                neutralDarkColorScheme()
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
        return
    }

    // Determine if system dynamic colors should be used (Android S+ and default theme color)
    val useSystemDynamicColor = (themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    // Select the appropriate color scheme generation method
    val baseColorScheme = if (useSystemDynamicColor) {
        // Use standard Material 3 dynamic color functions for system wallpaper colors
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        // Use materialKolor only when a specific seed color is provided
        rememberDynamicColorScheme(
            seedColor = themeColor, // themeColor is guaranteed non-default here
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = PaletteStyle.TonalSpot // Keep existing style
        )
    }

    // Apply pureBlack modification if needed, similar to original logic
    val colorScheme = remember(baseColorScheme, pureBlack, darkTheme, forceBlackBackground) {
        if (darkTheme && pureBlack) {
            baseColorScheme.pureBlack(true)
        } else if (darkTheme && forceBlackBackground) {
            baseColorScheme.blackholeSurface()
        } else {
            baseColorScheme
        }
    }

    // Use standard MaterialTheme instead of MaterialExpressiveTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Use the defined AppTypography
        content = content
    )
}

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

fun Bitmap.extractGradientColors(): List<Color> {
    val extractedColors = Palette.from(this)
        .maximumColorCount(64)
        .generate()
        .swatches
        .associate { it.rgb to it.population }

    val orderedColors = Score.score(extractedColors, 2, 0xff4285f4.toInt(), true)
        .sortedByDescending { Color(it).luminance() }

    return if (orderedColors.size >= 2)
        listOf(Color(orderedColors[0]), Color(orderedColors[1]))
    else
        listOf(Color(0xFF595959), Color(0xFF0D0D0D))
}

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

fun ColorScheme.blackholeSurface(): ColorScheme = copy(
    surface = Color.Black,
    background = Color.Black,
    surfaceVariant = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF1A1A1A),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainerLowest = Color.Black,
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF2A2A2A),
)

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
