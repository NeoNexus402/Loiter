package com.metrolist.music.ui.screens.settings.integrations

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.utils.ParsedFile
import com.metrolist.music.utils.ParsedPlaylist
import com.metrolist.music.viewmodels.PlaylistImportViewModel

private const val TUNE_MY_MUSIC_URL = "https://www.tunemymusic.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistImportScreen(
    navController: NavController,
    viewModel: PlaylistImportViewModel = hiltViewModel(),
) {
    val fileImportState by viewModel.fileImportState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            viewModel.onFileSelected(uri)
        }
    }

    PlaylistFileImportScreen(
        fileImportState = fileImportState,
        onPickFile = { filePickerLauncher.launch("text/*") },
        onImport = { parsed -> viewModel.importFileSongs(parsed) },
        onBack = { navController.navigateUp() },
        onReset = { viewModel.resetFileImport() },
        onNavigateToLibrary = {
            navController.navigate("library") {
                popUpTo(0) { inclusive = true }
            }
        },
    )
}

@Composable
private fun AuthTopBar(
    title: String,
    onBack: () -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack, onLongClick = onBack) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
        actions = {
            action?.invoke()
        },
    )
}

@Composable
private fun PlaylistFileImportScreen(
    fileImportState: PlaylistImportViewModel.FileImportState,
    onPickFile: () -> Unit,
    onImport: (ParsedFile) -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onNavigateToLibrary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current),
    ) {
        AuthTopBar(
            title = stringResource(R.string.playlist_import_file_title),
            onBack = onBack,
        )

        when (fileImportState) {
            is PlaylistImportViewModel.FileImportState.Idle -> {
                PlaylistFileImportInstructions(
                    onPickFile = onPickFile,
                )
            }

            is PlaylistImportViewModel.FileImportState.FileSelected -> {
                val parsed = fileImportState.parsed
                FileSelectedContent(
                    parsed = parsed,
                    onImport = { onImport(parsed) },
                )
            }

            is PlaylistImportViewModel.FileImportState.Importing -> {
                val progress = fileImportState
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(
                                R.string.playlist_import_progress,
                                progress.current,
                                progress.total,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (progress.total > 0) progress.current.toFloat() / progress.total else 0f
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            is PlaylistImportViewModel.FileImportState.Done -> {
                LaunchedEffect(Unit) {
                    onNavigateToLibrary()
                }
            }

            is PlaylistImportViewModel.FileImportState.Error -> {
                PlaylistErrorState(
                    message = fileImportState.message,
                    onRetry = onPickFile,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun FileSelectedContent(
    parsed: ParsedFile,
    onImport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "Found ${parsed.playlists.size} playlist(s) with ${parsed.playlists.sumOf { it.songs.size }} total songs",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            parsed.playlists.forEach { playlistGroup ->
                item(key = "header_${playlistGroup.name}") {
                    Text(
                        playlistGroup.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(
                    items = playlistGroup.songs.take(100),
                    key = { "${playlistGroup.name}_${it.title}" },
                ) { song ->
                    Text(
                        buildString {
                            append(song.title)
                            if (!song.artist.isNullOrBlank()) {
                                append(" — ")
                                append(song.artist)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
                if (playlistGroup.songs.size > 100) {
                    item(key = "more_${playlistGroup.name}") {
                        Text(
                            "… and ${playlistGroup.songs.size - 100} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(stringResource(R.string.playlist_import_file_create_playlist))
        }
    }
}

@Composable
private fun PlaylistErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current),
    ) {
        AuthTopBar(
            title = stringResource(R.string.playlist_import),
            onBack = onBack,
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun PlaylistFileImportInstructions(
    onPickFile: () -> Unit,
) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                stringResource(R.string.playlist_import_file_export_instructions_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // Prominent link to TuneMyMusic
        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TUNE_MY_MUSIC_URL))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    painterResource(R.drawable.download),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Go to TuneMyMusic.com")
            }
        }

        item {
            InstructionStep(
                step = 1,
                text = "Connect your music services (Spotify, Apple Music, etc.) on TuneMyMusic",
            )
        }
        item {
            InstructionStep(
                step = 2,
                text = "Select the playlists you want to export",
            )
        }
        item {
            InstructionStep(
                step = 3,
                text = "Choose CSV as the export format and download the file to your device",
            )
        }
        item {
            InstructionStep(
                step = 4,
                text = "Come back here and tap the button below to select your exported file",
            )
        }
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPickFile,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Icon(
                    painterResource(R.drawable.download),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.playlist_import_select_file))
            }
        }
    }
}

@Composable
private fun InstructionStep(step: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "$step.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
