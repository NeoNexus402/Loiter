package com.metrolist.music.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import coil3.compose.AsyncImage
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.db.entities.LyricsEntity
import com.metrolist.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    val barData by playerConnection.service.visualizerProcessor.bars.collectAsStateWithLifecycle()
    val accentIsDark = remember(dynamicAccent) {
        (0.2126f * dynamicAccent.red + 0.7152f * dynamicAccent.green + 0.0722f * dynamicAccent.blue) < 0.3f
    }
    val lyricsVizColor = if (accentIsDark) Color.White else dynamicAccent

    var showHeartAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            if (isPlaying) {
                duration = playerConnection.player.duration
                if (sliderPosition == null) {
                    position = playerConnection.player.currentPosition
                }
                delay(16)
            } else {
                delay(1000)
            }
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
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))
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
                        .clip(CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    playerConnection.toggleLike()
                                    showHeartAnimation = true
                                    coroutineScope.launch {
                                        delay(800)
                                        showHeartAnimation = false
                                    }
                                }
                            )
                        },
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

                    if (showHeartAnimation) {
                        val heartScale by animateFloatAsState(
                            targetValue = if (showHeartAnimation) 1f else 0f,
                            animationSpec = tween(300),
                            label = "heartScale",
                        )
                        Icon(
                            painter = painterResource(
                                if (isFavorite) R.drawable.favorite else R.drawable.favorite_border
                            ),
                            contentDescription = null,
                            tint = if (accentIsDark) Color.White else dynamicAccent,
                            modifier = Modifier.size(64.dp).graphicsLayer(
                                scaleX = heartScale,
                                scaleY = heartScale,
                                alpha = heartScale,
                            ),
                        )
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

            val lyricsText = remember(currentLyrics) {
                val entity = currentLyrics
                if (entity != null && entity.lyrics != LYRICS_NOT_FOUND) entity.lyrics else null
            }
            val showLyricsNow = showInlineLyrics && lyricsText != null

            val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsStateWithLifecycle()
            val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(max = 160.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = showLyricsNow,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "lyrics_swap",
                ) { showLyrics ->
                if (showLyrics) {
                    val text = lyricsText ?: return@AnimatedContent
                    LyricsSnippet(
                        lyrics = text,
                        progress = progress,
                        duration = duration,
                        accentColor = lyricsVizColor,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Visualizer(
                    barData = barData,
                    accentColor = lyricsVizColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    )
                }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    playerConnection.player.repeatMode = when (repeatMode) {
                        REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                        REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                        else -> REPEAT_MODE_OFF
                    }
                }) {
                    Icon(
                        painter = painterResource(
                            when (repeatMode) {
                                REPEAT_MODE_ALL -> R.drawable.repeat_on
                                REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                                else -> R.drawable.repeat
                            }
                        ),
                        contentDescription = null,
                        tint = if (repeatMode != REPEAT_MODE_OFF) dynamicAccent else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }

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

                IconButton(onClick = { playerConnection.player.shuffleModeEnabled = !shuffleEnabled }) {
                    Icon(
                        painter = painterResource(
                            if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle
                        ),
                        contentDescription = null,
                        tint = if (shuffleEnabled) dynamicAccent else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
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
private fun Visualizer(
    barData: List<Float>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val displayCount = barData.size
    val totalBars = displayCount * 2

    Canvas(modifier = modifier) {
        if (displayCount == 0) return@Canvas
        val barWidth = size.width / totalBars
        val gap = barWidth * 0.08f
        val actualBarWidth = barWidth - gap * 2
        val midY = size.height / 2f

        barData.forEachIndexed { index, h ->
            val barHeight = midY * h.coerceIn(0f, 1f)
            if (barHeight < 0.5f) return@forEachIndexed
            val alpha = (0.4f + 0.6f * h).coerceIn(0f, 1f)
            val color = accentColor.copy(alpha = alpha)

            val x = index * barWidth + gap
            drawRoundRect(
                color = color,
                topLeft = Offset(x, midY - barHeight),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2f, actualBarWidth / 2f),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x, midY),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2f, actualBarWidth / 2f),
            )

            val mirrorIndex = totalBars - 1 - index
            val mirrorX = mirrorIndex * barWidth + gap
            drawRoundRect(
                color = color,
                topLeft = Offset(mirrorX, midY - barHeight),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2f, actualBarWidth / 2f),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(mirrorX, midY),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2f, actualBarWidth / 2f),
            )
        }
    }
}

@Composable
private fun LyricsSnippet(
    lyrics: String,
    progress: Float,
    duration: Long,
    accentColor: Color,
    modifier: Modifier = Modifier,
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

    val lineStyle = MaterialTheme.typography.bodyMedium.copy(
        fontStyle = FontStyle.Italic,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 34.sp,
        textAlign = TextAlign.Center,
    )
    val lineHeightDp = with(LocalDensity.current) { 34.sp.toDp() }
    val verticalPadding = 12.dp

    AnimatedContent(
        targetState = currentLineIndex,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        modifier = modifier,
        label = "lyricsSnippet",
    ) { lineIndex ->
        val prev = parsedLines.getOrNull(lineIndex - 1)?.text
        val curr = parsedLines.getOrNull(lineIndex)?.text ?: return@AnimatedContent
        val nxt = parsedLines.getOrNull(lineIndex + 1)?.text

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (prev != null) {
                Text(
                    text = prev,
                    style = lineStyle,
                    color = accentColor.copy(alpha = 0.35f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = verticalPadding),
                )
            } else {
                Spacer(modifier = Modifier.height(lineHeightDp + verticalPadding * 2))
            }

            Text(
                text = curr,
                style = lineStyle,
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding),
            )

            if (nxt != null) {
                Text(
                    text = nxt,
                    style = lineStyle,
                    color = accentColor.copy(alpha = 0.35f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = verticalPadding),
                )
            } else {
                Spacer(modifier = Modifier.height(lineHeightDp + verticalPadding * 2))
            }
        }
    }
}
