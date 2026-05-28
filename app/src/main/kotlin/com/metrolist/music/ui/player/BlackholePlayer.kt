package com.metrolist.music.ui.player

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.ui.component.BottomSheetState
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.menu.PlayerMenu
import com.metrolist.music.utils.makeTimeString
import kotlinx.coroutines.delay

@Composable
fun BlackholePlayerContent(
    playerConnection: PlayerConnection,
    navController: androidx.navigation.NavController,
    showInlineLyrics: Boolean,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier,
    blackholeAccentColor: Color = Color(0xFF1DB954),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current

    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()
    val shuffleMode = playerConnection.player.shuffleModeEnabled

    val currentSong by playerConnection.currentSong.collectAsStateWithLifecycle(initialValue = null)
    val isEpisode = currentSong?.song?.isEpisode == true
    val isFavorite = if (isEpisode) currentSong?.song?.inLibrary != null else currentSong?.song?.liked == true

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            duration = playerConnection.player.duration
            if (sliderPosition == null) {
                position = playerConnection.player.currentPosition
            }
            delay(200)
        }
    }

    val title = mediaMetadata?.title ?: "Unknown"
    val artist = mediaMetadata?.artists?.joinToString(", ") { it.name } ?: "Unknown"
    val displayPosition = sliderPosition ?: position
    val progress = if (duration > 0) (displayPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!showInlineLyrics) {
                Spacer(modifier = Modifier.width(32.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.titleMedium,
                        color = blackholeAccentColor,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            FilledIconButton(
                onClick = {
                    menuState.show {
                        PlayerMenu(
                            mediaMetadata = mediaMetadata,
                            navController = navController,
                            playerBottomSheetState = playerBottomSheetState,
                            onShowDetailsDialog = {
                                mediaMetadata?.id?.let {
                                    navController.navigate("now_playing_details/$it")
                                }
                            },
                            onDismiss = menuState::dismiss,
                        )
                    }
                },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White,
                ),
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = blackholeAccentColor,
                )
            }
        }

        if (!showInlineLyrics) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Thumbnail(
                    sliderPositionProvider = { sliderPosition },
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .aspectRatio(1f),
                    isPlayerExpanded = { playerBottomSheetState.isExpanded },
                    isLandscape = true,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                InlineLyricsView(
                    mediaMetadata = mediaMetadata,
                    showLyrics = true,
                    positionProvider = { position },
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
            ),
            color = Color(0xFF888888),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        if (!showInlineLyrics) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { playerConnection.toggleLike() },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFavorite) R.drawable.favorite else R.drawable.favorite_border
                        ),
                        contentDescription = null,
                        tint = if (isFavorite) blackholeAccentColor else Color(0xFF888888),
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(
                    onClick = {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaMetadata?.id}")
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = null,
                        tint = blackholeAccentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = "${makeTimeString(displayPosition)} / ${makeTimeString(duration)}",
            style = MaterialTheme.typography.bodySmall,
            color = blackholeAccentColor,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val seekPos = (fraction * duration).toLong()
                        playerConnection.player.seekTo(seekPos)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            sliderPosition = (fraction * duration).toLong()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            sliderPosition = (fraction * duration).toLong()
                        },
                        onDragEnd = {
                            sliderPosition?.let { playerConnection.player.seekTo(it) }
                            sliderPosition = null
                        },
                        onDragCancel = {
                            sliderPosition = null
                        },
                    )
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackColor = Color(0xFF333333)
                val filledColor = blackholeAccentColor
                val seekProgress = progress

                drawLine(trackColor, start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = size.height)
                if (seekProgress > 0.001f) {
                    drawLine(filledColor, start = Offset(0f, center.y), end = Offset(size.width * seekProgress, center.y), strokeWidth = size.height)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = { playerConnection.player.shuffleModeEnabled = !playerConnection.player.shuffleModeEnabled },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (shuffleMode) blackholeAccentColor else Color(0xFF333333),
                    contentColor = Color.White,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            FilledIconButton(
                onClick = { playerConnection.seekToPrevious() },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(blackholeAccentColor),
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
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            FilledIconButton(
                onClick = { playerConnection.seekToNext() },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }

            FilledIconButton(
                onClick = {
                    val modes = listOf(
                        Player.REPEAT_MODE_OFF,
                        Player.REPEAT_MODE_ALL,
                        Player.REPEAT_MODE_ONE,
                    )
                    val nextIndex = (modes.indexOf(repeatMode) + 1) % modes.size
                    playerConnection.player.repeatMode = modes[nextIndex]
                },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (repeatMode != Player.REPEAT_MODE_OFF) blackholeAccentColor else Color(0xFF333333),
                    contentColor = Color.White,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.repeat),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
