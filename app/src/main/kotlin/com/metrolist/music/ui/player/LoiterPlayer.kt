package com.metrolist.music.ui.player

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.db.entities.LyricsEntity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.metrolist.music.lyrics.LyricsUtils
import com.metrolist.music.lyrics.lyricsTextLooksSynced
import com.metrolist.music.ui.component.BottomSheetState
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.menu.PlayerMenu
import com.metrolist.music.ui.theme.LocalDynamicAccentColor
import kotlinx.coroutines.delay

private val albumArtFraction = 0.85f
private val ringWidth = 5.dp

@Composable
fun LoiterPlayerContent(
    playerConnection: com.metrolist.music.playback.PlayerConnection,
    navController: androidx.navigation.NavController,
    showInlineLyrics: Boolean,
    onToggleLyrics: () -> Unit,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val menuState = LocalMenuState.current
    val dynamicAccent = LocalDynamicAccentColor.current

    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val currentSong by playerConnection.currentSong.collectAsStateWithLifecycle(initialValue = null)
    val currentLyrics by playerConnection.currentLyrics.collectAsStateWithLifecycle(initialValue = null)
    val isEpisode = currentSong?.song?.isEpisode == true
    val isFavorite = if (isEpisode) currentSong?.song?.inLibrary != null else currentSong?.song?.liked == true

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            duration = playerConnection.player.duration
            if (sliderPosition == null) {
                position = playerConnection.player.currentPosition
            }
            delay(200)
        }
    }

    val metadata = mediaMetadata
    LaunchedEffect(metadata?.id, currentLyrics) {
        if (metadata != null && currentLyrics == null) {
            delay(500)
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val entryPoint =
                        EntryPointAccessors.fromApplication(
                            context.applicationContext,
                            com.metrolist.music.di.LyricsHelperEntryPoint::class.java,
                        )
                    val lyricsHelper = entryPoint.lyricsHelper()
                    val fetchedLyricsWithProvider = lyricsHelper.getLyrics(metadata)
                    database.query {
                        upsert(LyricsEntity(metadata.id, fetchedLyricsWithProvider.lyrics, fetchedLyricsWithProvider.provider))
                    }
                } catch (_: Exception) { }
            }
        }
    }

    val title = mediaMetadata?.title ?: "Unknown"
    val artist = mediaMetadata?.artists?.joinToString(", ") { it.name } ?: "Unknown"
    val thumbnailUrl = mediaMetadata?.thumbnailUrl
    val displayPosition = sliderPosition ?: position
    val progress = if (duration > 0) (displayPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        TopBar(
            dynamicAccent = dynamicAccent,
            onBack = { playerBottomSheetState.collapseSoft() },
            onMenuClick = {
                menuState.show {
                    PlayerMenu(
                        mediaMetadata = mediaMetadata,
                        navController = navController,
                        playerBottomSheetState = playerBottomSheetState,
                        onShowDetailsDialog = {
                            mediaMetadata?.id?.let { id ->
                                navController.navigate("now_playing_details/$id")
                            }
                        },
                        onDismiss = menuState::dismiss,
                    )
                }
            },
            isFavorite = isFavorite,
            onFavoriteClick = { playerConnection.toggleLike() },
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(albumArtFraction)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressRing(
                    progress = progress,
                    accentColor = dynamicAccent,
                    onSeek = { newProgress ->
                        val seekPosition = (newProgress * duration).toLong()
                        if (seekPosition >= 0 && duration > 0) {
                            playerConnection.player.seekTo(seekPosition)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ringWidth + 8.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (thumbnailUrl != null) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.music_note),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = artist,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            val lyricsEntity = currentLyrics
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (showInlineLyrics && lyricsEntity != null && lyricsEntity.lyrics != com.metrolist.music.db.entities.LyricsEntity.LYRICS_NOT_FOUND) {
                    LyricsSnippet(
                        lyrics = lyricsEntity.lyrics,
                        progress = progress,
                        duration = duration,
                        accentColor = dynamicAccent,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { playerConnection.seekToPrevious() }) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(dynamicAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = {
                        if (isPlaying) playerConnection.player.pause()
                        else playerConnection.player.play()
                    }) {
                        Icon(
                            painter = painterResource(
                                if (isPlaying) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                IconButton(onClick = { playerConnection.seekToNext() }) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    dynamicAccent: Color,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onFavoriteClick,
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) R.drawable.favorite else R.drawable.favorite_border
                ),
                contentDescription = null,
                tint = if (isFavorite) dynamicAccent else Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    accentColor: Color,
    onSeek: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .drawBehind {
                val stroke = ringWidth.toPx()
                val padding = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val arcTopLeft = Offset(padding, padding)
                val sweep = progress * 360f

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )

                if (sweep > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = change.position.x - center.x
                    val dy = change.position.y - center.y
                    val angle = (Math.atan2(dy.toDouble(), dx.toDouble()) + Math.PI / 2).let { if (it < 0) it + 2 * Math.PI else it }
                    onSeek((angle / (2 * Math.PI)).toFloat().coerceIn(0f, 1f))
                }
            },
    )
}

@Composable
private fun LyricsSnippet(
    lyrics: String,
    progress: Float,
    duration: Long,
    accentColor: Color,
) {
    val isSynced = remember(lyrics) { lyricsTextLooksSynced(lyrics) }
    val parsedLines = remember(lyrics) {
        LyricsUtils.parseLyrics(lyrics).filter { it.text.isNotBlank() }
    }

    if (parsedLines.isEmpty()) return

    val currentPositionMs = (progress * duration).toLong()
    val currentLineIndex = remember(parsedLines, isSynced, currentPositionMs, duration) {
        if (isSynced && duration > 0) {
            LyricsUtils.findCurrentLineIndex(parsedLines, currentPositionMs).coerceAtLeast(0)
        } else {
            ((parsedLines.size * progress).toInt()).coerceIn(0, parsedLines.size - 1)
        }
    }

    val currentText = parsedLines.getOrNull(currentLineIndex)?.text ?: return
    val previousText = parsedLines.getOrNull(currentLineIndex - 1)?.text
    val nextText = parsedLines.getOrNull(currentLineIndex + 1)?.text

    val lineStyle = MaterialTheme.typography.bodyMedium.copy(
        fontStyle = FontStyle.Italic,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 31.2.sp,
        textAlign = TextAlign.Center,
    )
    val mutedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val lineHeightDp = with(LocalDensity.current) { 31.2.sp.toDp() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (previousText != null) {
            Text(
                text = previousText,
                style = lineStyle,
                color = mutedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Spacer(modifier = Modifier.height(lineHeightDp))
        }

        Text(
            text = currentText,
            style = lineStyle,
            color = accentColor,
            modifier = Modifier.fillMaxWidth(),
        )

        if (nextText != null) {
            Text(
                text = nextText,
                style = lineStyle,
                color = mutedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Spacer(modifier = Modifier.height(lineHeightDp))
        }
    }
}
