package com.metrolist.music.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.constants.GridAppearance
import com.metrolist.music.constants.GridAppearanceKey
import com.metrolist.music.utils.rememberEnumPreference
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.metrolist.music.BuildConfig
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.ReleaseNotesCard
import com.metrolist.music.ui.theme.LocalDynamicAccentColor
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.Updater

@Composable
fun LoiterSettingsScreen(
    navController: NavController,
    latestVersionName: String,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val dynamicAccent = LocalDynamicAccentColor.current
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasAndroidAuto = remember {
        try {
            context.packageManager.getPackageInfo("com.google.android.projection.gearhead", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        )

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Theme Section — Dark Mode Switcher
        val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, DarkMode.AUTO)
        LoiterSettingsSection(title = "Theme") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        DarkMode.OFF to "Light",
                        DarkMode.AUTO to "System",
                        DarkMode.ON to "Dark",
                    ).forEach { (mode, label) ->
                        val isSel = darkMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSel) dynamicAccent else Color.Transparent)
                                .clickable { onDarkModeChange(mode) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Grid Appearance Section
        val (gridAppearance, onGridAppearanceChange) = rememberEnumPreference(GridAppearanceKey, GridAppearance.DEFAULT)
        LoiterSettingsSection(title = "Grid Appearance") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(
                        GridAppearance.DEFAULT to "Default",
                        GridAppearance.ALL_ROUND to "AllRound",
                        GridAppearance.BOX to "Box",
                        GridAppearance.SQUARE to "Square",
                        GridAppearance.INVERTED to "Inverted",
                    ).forEach { (mode, label) ->
                        val isSel = gridAppearance == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSel) dynamicAccent else Color.Transparent)
                                .clickable { onGridAppearanceChange(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // User Interface Section
        LoiterSettingsSection(title = stringResource(R.string.settings_section_ui)) {
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.palette),
                title = stringResource(R.string.appearance),
                onClick = { navController.navigate("settings/appearance") },
            )
        }

        // Player & Content Section
        LoiterSettingsSection(title = stringResource(R.string.settings_section_player_content)) {
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.play),
                title = stringResource(R.string.player_and_audio),
                onClick = { navController.navigate("settings/player") },
            )
            Spacer(Modifier.height(8.dp))
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.language),
                title = stringResource(R.string.content),
                onClick = { navController.navigate("settings/content") },
            )
            Spacer(Modifier.height(8.dp))
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.translate),
                title = stringResource(R.string.ai_lyrics_translation),
                onClick = { navController.navigate("settings/ai") },
            )
        }

        // Android Auto Section
        if (hasAndroidAuto) {
            LoiterSettingsSection(title = "Android Auto") {
                LoiterSettingsCard(
                    icon = ImageVector.vectorResource(R.drawable.ic_android_auto),
                    title = stringResource(R.string.android_auto),
                    onClick = { navController.navigate("settings/android_auto") },
                )
            }
        }

        // Privacy & Security Section
        LoiterSettingsSection(title = stringResource(R.string.settings_section_privacy)) {
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.security),
                title = stringResource(R.string.privacy),
                onClick = { navController.navigate("settings/privacy") },
            )
        }

        // Storage & Data Section
        LoiterSettingsSection(title = stringResource(R.string.settings_section_storage)) {
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.storage),
                title = stringResource(R.string.storage),
                onClick = { navController.navigate("settings/storage") },
            )
            Spacer(Modifier.height(8.dp))
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.restore),
                title = stringResource(R.string.backup_restore),
                onClick = { navController.navigate("settings/backup_restore") },
            )
        }

        // System & About Section
        LoiterSettingsSection(title = stringResource(R.string.settings_section_system)) {
            if (isAndroid12OrLater) {
                LoiterSettingsCard(
                    icon = ImageVector.vectorResource(R.drawable.link),
                    title = stringResource(R.string.default_links),
                    onClick = {
                        try {
                            val intent = Intent(
                                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                "package:${context.packageName}".toUri()
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            when (e) {
                                is ActivityNotFoundException, is SecurityException -> {
                                    Toast.makeText(
                                        context,
                                        R.string.open_app_settings_error,
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                                else -> {
                                    Toast.makeText(
                                        context,
                                        R.string.open_app_settings_error,
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
            }
            if (BuildConfig.UPDATER_AVAILABLE) {
                LoiterSettingsCard(
                    icon = ImageVector.vectorResource(R.drawable.update),
                    title = stringResource(R.string.updater),
                    onClick = { navController.navigate("settings/updater") },
                )
                Spacer(Modifier.height(8.dp))
            }
            val showChangelog = com.metrolist.music.LocalChangelogState.current
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.newspaper),
                title = stringResource(R.string.changelog),
                onClick = { showChangelog.value = true },
            )
            Spacer(Modifier.height(8.dp))
            LoiterSettingsCard(
                icon = ImageVector.vectorResource(R.drawable.info),
                title = stringResource(R.string.about),
                onClick = { navController.navigate("settings/about") },
            )
            if (BuildConfig.UPDATER_AVAILABLE && latestVersionName != BuildConfig.VERSION_NAME) {
                val releaseInfo = Updater.getCachedLatestRelease()
                val downloadUrl = releaseInfo?.let { Updater.getDownloadUrlForCurrentVariant(it) }
                if (downloadUrl != null) {
                    Spacer(Modifier.height(8.dp))
                    LoiterSettingsCard(
                        icon = ImageVector.vectorResource(R.drawable.update),
                        title = stringResource(R.string.new_version_available),
                        subtitle = latestVersionName,
                        showBadge = true,
                        onClick = { uriHandler.openUri(downloadUrl) },
                    )
                }
            }
        }

        if (BuildConfig.UPDATER_AVAILABLE && latestVersionName != BuildConfig.VERSION_NAME) {
            Spacer(Modifier.height(16.dp))
            ReleaseNotesCard()
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LoiterSettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val dynamicAccent = LocalDynamicAccentColor.current

    Spacer(Modifier.height(8.dp))
    Text(
        text = title,
        color = dynamicAccent,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
    content()
}

@Composable
private fun LoiterSettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    showBadge: Boolean = false,
    onClick: () -> Unit,
) {
    val dynamicAccent = LocalDynamicAccentColor.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(dynamicAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = dynamicAccent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(dynamicAccent),
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.arrow_forward),
                contentDescription = null,
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
