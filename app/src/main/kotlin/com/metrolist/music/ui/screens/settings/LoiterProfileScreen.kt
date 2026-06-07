package com.metrolist.music.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.ui.component.AccountSettingsDialog
import com.metrolist.music.ui.theme.LocalDynamicAccentColor
import com.metrolist.music.viewmodels.HomeViewModel

@Composable
fun LoiterProfileScreen(
    navController: NavController,
    activity: Activity,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val dynamicAccent = LocalDynamicAccentColor.current
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsStateWithLifecycle()
    var showAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        )

        Spacer(Modifier.height(24.dp))

        // Account Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(dynamicAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (accountImageUrl != null) {
                        AsyncImage(
                            model = accountImageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.person),
                            contentDescription = null,
                            tint = dynamicAccent,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Menu Items
        ProfileMenuItem(
            icon = ImageVector.vectorResource(R.drawable.settings),
            title = stringResource(R.string.settings),
            onClick = { navController.navigate("settings") },
        )

        Spacer(Modifier.height(8.dp))

        ProfileMenuItem(
            icon = ImageVector.vectorResource(R.drawable.group_outlined),
            title = stringResource(R.string.together),
            onClick = { navController.navigate("listen_together") },
        )

        Spacer(Modifier.height(8.dp))

        ProfileMenuItem(
            icon = ImageVector.vectorResource(R.drawable.history),
            title = stringResource(R.string.history),
            onClick = { navController.navigate("history") },
        )

        Spacer(Modifier.height(8.dp))

        ProfileMenuItem(
            icon = ImageVector.vectorResource(R.drawable.login),
            title = stringResource(R.string.account),
            onClick = { showAccountDialog = true },
        )

        Spacer(Modifier.height(8.dp))

        ProfileMenuItem(
            icon = ImageVector.vectorResource(R.drawable.upload),
            title = stringResource(R.string.import_playlist),
            onClick = { navController.navigate("settings/integrations/import") },
        )

        Spacer(Modifier.height(8.dp))

        ProfileMenuItem(
            icon = ImageVector.vectorResource(R.drawable.info),
            title = stringResource(R.string.about),
            onClick = { navController.navigate("settings/about") },
        )

        Spacer(Modifier.height(24.dp))
    }

    if (showAccountDialog) {
        AccountSettingsDialog(
            navController = navController,
            onDismiss = {
                showAccountDialog = false
                homeViewModel.refresh()
            },
            latestVersionName = com.metrolist.music.BuildConfig.VERSION_NAME,
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    val dynamicAccent = LocalDynamicAccentColor.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
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

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
