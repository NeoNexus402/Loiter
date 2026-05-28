package com.metrolist.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Immutable

enum class LayoutTheme(
    val displayName: String,
    val description: String,
    val isComingSoon: Boolean = false,
) {
    METROLIST("Metrolist", "The default Metrolist experience — Material 3 with dynamic colors"),
    LOITER("Loiter", "The new Loiter experience — coming soon", isComingSoon = true),
    YT_MUSIC("YouTube Music", "Inspired by YouTube Music — full-bleed thumbnails, compact controls", isComingSoon = true),
    SPOTIFY("Spotify", "Inspired by Spotify — bold typography, dark accents, gradient player", isComingSoon = true),
    BLACKHOLE("Blackhole", "Inspired by Blackhole — large artwork, heavy blur, wave animations"),
}

enum class NavBarStyle {
    DEFAULT,
    COMPACT,
    PILL,
}

enum class MiniPlayerLayout {
    DEFAULT,
    OVERLAY,
}

enum class PlayerLayout {
    DEFAULT,
    MODERN,
}

@Immutable
data class LayoutThemeConfig(
    val theme: LayoutTheme,
    val fontFamily: FontFamily,
    val typography: Typography,
    val playerCornerRadius: Dp,
    val cardCornerRadius: Dp,
    val useCompactPlayerControls: Boolean,
    val showGradientOverlay: Boolean,
    val accentColor: Color?,
    val playPauseButtonSize: Dp,
    val controlButtonSize: Dp,
    val songTitleStyle: TextStyle,
    val artistStyle: TextStyle,
    val defaultPlayerBackground: String,
    val navBarStyle: NavBarStyle,
    val useAlbumArtBorder: Boolean,
    val forceDarkTheme: Boolean = false,
    val miniPlayerLayout: MiniPlayerLayout = MiniPlayerLayout.DEFAULT,
    val playerLayout: PlayerLayout = PlayerLayout.DEFAULT,
    val forceBlackBackground: Boolean = false,
    val lockDynamicTheme: Boolean = false,
    val lockNewPlayerDesign: Boolean = false,
    val lockNewMiniPlayerDesign: Boolean = false,
    val lockPlayerStyle: Boolean = false,
    val lockSliderStyle: Boolean = false,
    val lockSlimNavBar: Boolean = false,
    val lockThemeOverview: Boolean = false,
    val seedColor: Color? = null,
) {
    val effectiveAccentColor: Color?
        get() = accentColor ?: seedColor
}

val LocalLayoutTheme = compositionLocalOf(structuralEqualityPolicy()) { LayoutTheme.METROLIST }
val LocalLayoutThemeConfig = compositionLocalOf(structuralEqualityPolicy()) { metrolistThemeConfig }

private val loiterPlayerTitleStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp,
)

private val loiterArtistStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
)

val metrolistThemeConfig = LayoutThemeConfig(
    theme = LayoutTheme.METROLIST,
    fontFamily = FontFamily.Default,
    typography = AppTypography,
    playerCornerRadius = 0.dp,
    cardCornerRadius = 16.dp,
    useCompactPlayerControls = false,
    showGradientOverlay = false,
    accentColor = null,
    playPauseButtonSize = 72.dp,
    controlButtonSize = 32.dp,
    songTitleStyle = loiterPlayerTitleStyle,
    artistStyle = loiterArtistStyle,
    defaultPlayerBackground = "DEFAULT",
    navBarStyle = NavBarStyle.DEFAULT,
    useAlbumArtBorder = false,
)

val ytMusicThemeConfig = LayoutThemeConfig(
    theme = LayoutTheme.YT_MUSIC,
    fontFamily = FontFamily.Default,
    typography = AppTypography,
    playerCornerRadius = 0.dp,
    cardCornerRadius = 8.dp,
    useCompactPlayerControls = true,
    showGradientOverlay = true,
    accentColor = Color(0xFFFF0033),
    playPauseButtonSize = 56.dp,
    controlButtonSize = 28.dp,
    songTitleStyle = loiterPlayerTitleStyle.copy(fontSize = 18.sp),
    artistStyle = loiterArtistStyle.copy(fontSize = 13.sp),
    defaultPlayerBackground = "DEFAULT",
    navBarStyle = NavBarStyle.COMPACT,
    useAlbumArtBorder = false,
    lockDynamicTheme = true,
    lockPlayerStyle = true,
    lockSliderStyle = true,
    lockSlimNavBar = true,
    lockThemeOverview = true,
    seedColor = Color(0xFFFF0033),
)

private val spotifyTitleStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.25).sp,
)

private val spotifyArtistStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.15.sp,
)

val spotifyThemeConfig = LayoutThemeConfig(
    theme = LayoutTheme.SPOTIFY,
    fontFamily = FontFamily.Default,
    typography = Typography(
        displayLarge = AppTypography.displayLarge.copy(fontWeight = FontWeight.Bold),
        displayMedium = AppTypography.displayMedium.copy(fontWeight = FontWeight.Bold),
        displaySmall = AppTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = AppTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = AppTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
        titleSmall = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
        bodyLarge = AppTypography.bodyLarge,
        bodyMedium = AppTypography.bodyMedium,
        bodySmall = AppTypography.bodySmall,
        labelLarge = AppTypography.labelLarge,
        labelMedium = AppTypography.labelMedium,
        labelSmall = AppTypography.labelSmall,
    ),
    playerCornerRadius = 12.dp,
    cardCornerRadius = 8.dp,
    useCompactPlayerControls = true,
    showGradientOverlay = true,
    accentColor = Color(0xFF1DB954),
    playPauseButtonSize = 80.dp,
    controlButtonSize = 36.dp,
    songTitleStyle = spotifyTitleStyle,
    artistStyle = spotifyArtistStyle,
    defaultPlayerBackground = "GRADIENT",
    navBarStyle = NavBarStyle.PILL,
    useAlbumArtBorder = true,
    forceDarkTheme = true,
    lockDynamicTheme = true,
    lockPlayerStyle = true,
    lockSliderStyle = true,
    lockSlimNavBar = true,
    lockThemeOverview = true,
    seedColor = Color(0xFF1DB954),
)

private val blackholeTitleStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
)

private val blackholeArtistStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
)

val blackholeThemeConfig = LayoutThemeConfig(
    theme = LayoutTheme.BLACKHOLE,
    fontFamily = FontFamily.Default,
    typography = AppTypography,
    playerCornerRadius = 24.dp,
    cardCornerRadius = 24.dp,
    useCompactPlayerControls = true,
    showGradientOverlay = true,
    accentColor = null,
    playPauseButtonSize = 64.dp,
    controlButtonSize = 30.dp,
    songTitleStyle = blackholeTitleStyle,
    artistStyle = blackholeArtistStyle,
    defaultPlayerBackground = "DEFAULT",
    navBarStyle = NavBarStyle.PILL,
    useAlbumArtBorder = true,
    forceDarkTheme = true,
    miniPlayerLayout = MiniPlayerLayout.OVERLAY,
    playerLayout = PlayerLayout.MODERN,
    forceBlackBackground = true,
    lockDynamicTheme = true,
    lockNewPlayerDesign = true,
    lockNewMiniPlayerDesign = true,
    lockPlayerStyle = true,
    lockSliderStyle = true,
    lockSlimNavBar = true,
    lockThemeOverview = true,
    seedColor = null,
)

fun configForTheme(theme: LayoutTheme, blackholeSeedColor: Color? = null): LayoutThemeConfig = when (theme) {
    LayoutTheme.METROLIST -> metrolistThemeConfig
    LayoutTheme.LOITER -> metrolistThemeConfig
    LayoutTheme.YT_MUSIC -> ytMusicThemeConfig
    LayoutTheme.SPOTIFY -> spotifyThemeConfig
    LayoutTheme.BLACKHOLE -> blackholeThemeConfig.copy(
        seedColor = blackholeSeedColor ?: Color(0xFF1DB954),
        // accentColor must remain null for Blackhole so the user's chosen color
        // does NOT leak into global UI elements (headers, nav bars, sliders, etc.).
        // The colour is instead passed explicitly to BlackholePlayerContent / BlackholeMiniPlayer.
        accentColor = null,
    )
}
