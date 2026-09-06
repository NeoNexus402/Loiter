package com.metrolist.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.ListItemHeight
import com.metrolist.music.db.entities.LocalItem
import com.metrolist.music.db.entities.Song
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.AlbumGridItem
import com.metrolist.music.ui.component.ArtistGridItem
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.component.RandomizeGridItem
import com.metrolist.music.ui.component.SongGridItem
import com.metrolist.music.ui.component.SongListItem
import com.metrolist.music.ui.component.SpeedDialGridItem
import com.metrolist.music.ui.component.YouTubeGridItem
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.theme.LocalDynamicAccentColor
import com.metrolist.music.ui.utils.resize
import com.metrolist.music.viewmodels.CommunityPlaylistItem
import com.metrolist.music.viewmodels.DailyDiscoverItem
import com.metrolist.music.viewmodels.HomeViewModel

@Composable
fun LoiterHomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val quickPicks by homeViewModel.quickPicks.collectAsStateWithLifecycle()
    val dailyDiscover by homeViewModel.dailyDiscover.collectAsStateWithLifecycle()
    val homePage by homeViewModel.homePage.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val keepListening by homeViewModel.keepListening.collectAsStateWithLifecycle()
    val forgottenFavorites by homeViewModel.forgottenFavorites.collectAsStateWithLifecycle()
    val similarRecommendations by homeViewModel.similarRecommendations.collectAsStateWithLifecycle()
    val accountPlaylists by homeViewModel.accountPlaylists.collectAsStateWithLifecycle()
    val speedDialItems by homeViewModel.speedDialItems.collectAsStateWithLifecycle()
    val communityPlaylists by homeViewModel.communityPlaylists.collectAsStateWithLifecycle()
    val explorePage by homeViewModel.explorePage.collectAsStateWithLifecycle()
    val selectedMoodGenre by homeViewModel.selectedMoodGenre.collectAsStateWithLifecycle()
    val filteredMoodContent by homeViewModel.filteredMoodContent.collectAsStateWithLifecycle()
    val accountName by homeViewModel.accountName.collectAsStateWithLifecycle()
    val homeLoading = isLoading && quickPicks.isNullOrEmpty()
    val scope = rememberCoroutineScope()

    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { homeViewModel.loadHomeData() }

    val speedDialTitle = stringResource(R.string.speed_dial)
    val quickPicksTitle = stringResource(R.string.quick_picks)
    val dailyDiscoverTitle = stringResource(R.string.your_daily_discover)
    val keepListeningTitle = stringResource(R.string.keep_listening)
    val mixesTitle = stringResource(R.string.mixes)
    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
    val communityTitle = stringResource(R.string.from_the_community)
    val moodGenresTitle = stringResource(R.string.mood_and_genres)

    var selectedMoodGenreIndex by remember { mutableIntStateOf(-1) }

    val dynamicAccent = LocalDynamicAccentColor.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LoiterAnimatedArt(accentColor = dynamicAccent)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(windowInsets)
                .padding(top = 16.dp),
        ) {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = { homeViewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                item(key = "loiter_branding") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Loiter",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = dynamicAccent,
                        )
                        Text(
                            text = "Your Time, Your Music",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Welcome, ${accountName}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (!quickPicks.isNullOrEmpty()) {
                    val songs = quickPicks!!
                    item(key = "qp_title") {
                        NavigationTitle(
                            title = quickPicksTitle,
                            onPlayAllClick = {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = quickPicksTitle,
                                        items = songs.map { it.toMediaItem() },
                                    ),
                                )
                            },
                        )
                    }
                    item(key = "qp_content") {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(4),
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * 4),
                        ) {
                            items(
                                items = songs.distinctBy { it.id },
                                key = { "qp_${it.id}" },
                            ) { song ->
                                SongListItem(
                                    song = song,
                                    isActive = song.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    isSwipeable = false,
                                    modifier = Modifier
                                        .width(240.dp)
                                        .combinedClickable(
                                            onClick = {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        items = songs.map { it.toMediaItem() },
                                                        startIndex = songs.indexOf(song).coerceAtLeast(0),
                                                    ),
                                                )
                                            },
                                        ),
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (selectedMoodGenre != null && filteredMoodContent != null) {
                    item(key = "filtered_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = selectedMoodGenre!!.title.replace("\n", " "),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = dynamicAccent,
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(dynamicAccent.copy(alpha = 0.15f))
                                    .clickable {
                                        selectedMoodGenreIndex = -1
                                        homeViewModel.selectMoodGenre(null)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "Clear",
                                    color = dynamicAccent,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                    filteredMoodContent!!.items.forEachIndexed { secIdx, section ->
                        val secItems = section.items.take(15)
                        if (secItems.isNotEmpty()) {
                            item(key = "mood_sec_title_$secIdx") {
                                NavigationTitle(title = section.title ?: "")
                            }
                            item(key = "mood_sec_content_$secIdx") {
                                val isSongsOnly = secItems.all { it is SongItem }
                                if (isSongsOnly) {
                                    LazyHorizontalGrid(
                                        rows = GridCells.Fixed(4),
                                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ListItemHeight * 4),
                                    ) {
                                        items(secItems, key = { it.id }) { item ->
                                            YouTubeListItem(
                                                item = item,
                                                isActive = item.id == mediaMetadata?.id,
                                                isPlaying = isPlaying,
                                                isSwipeable = false,
                                                modifier = Modifier
                                                    .width(240.dp)
                                                    .combinedClickable(
                                                        onClick = {
                                                            if (item is SongItem) {
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue(
                                                                        item.endpoint ?: WatchEndpoint(videoId = item.id),
                                                                        item.toMediaMetadata(),
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                    ),
                                            )
                                        }
                                    }
                                } else {
                                    LazyRow(
                                        contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                    ) {
                                        items(secItems, key = { it.id }) { item ->
                                            YouTubeGridItem(
                                                item = item,
                                                isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                                isPlaying = isPlaying,
                                                thumbnailRatio = 1f,
                                                modifier = Modifier
                                                    .width(160.dp)
                                                    .clickable {
                                                        when (item) {
                                                            is SongItem -> playerConnection.playQueue(
                                                                YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()),
                                                            )
                                                            is AlbumItem -> navController.navigate("album/${item.id}")
                                                            is ArtistItem -> navController.navigate("artist/${item.id}")
                                                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                            else -> {}
                                                        }
                                                    },
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    item(key = "mood_divider") {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (speedDialItems.isNotEmpty()) {
                    item(key = "sd_title") {
                        NavigationTitle(title = speedDialTitle)
                    }
                    item(key = "sd_content") {
                        SpeedDialSection(
                            items = speedDialItems,
                            navController = navController,
                            playerConnection = playerConnection,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!dailyDiscover.isNullOrEmpty()) {
                    item(key = "dd_title") {
                        NavigationTitle(
                            title = dailyDiscoverTitle,
                            onPlayAllClick = {
                                val queueItems = dailyDiscover!!.mapNotNull {
                                    (it.recommendation as? SongItem)?.toMediaMetadata()
                                }
                                if (queueItems.isNotEmpty()) {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = dailyDiscoverTitle,
                                            items = queueItems.map { it.toMediaItem() },
                                        ),
                                    )
                                }
                            },
                        )
                    }
                    item(key = "dd_content") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .padding(horizontal = 16.dp),
                        ) {
                            val carouselState = rememberCarouselState { dailyDiscover!!.size }
                            HorizontalMultiBrowseCarousel(
                                state = carouselState,
                                preferredItemWidth = 320.dp,
                                itemSpacing = 16.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp),
                            ) { i ->
                                val item = dailyDiscover!![i]
                                DailyDiscoverCard(
                                    dailyDiscover = item,
                                    onClick = {
                                        val song = item.recommendation as? SongItem
                                        val mm = song?.toMediaMetadata()
                                        if (mm != null) {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    song.endpoint ?: WatchEndpoint(videoId = song.id),
                                                    mm,
                                                ),
                                            )
                                        }
                                    },
                                    navController = navController,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!keepListening.isNullOrEmpty()) {
                    item(key = "kl_title") {
                        NavigationTitle(title = keepListeningTitle)
                    }
                    item(key = "kl_content") {
                        val rows = if (keepListening!!.size > 6) 2 else 1
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(rows),
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((128.dp + 64.dp) * rows),
                        ) {
                            items(keepListening!!, key = { it.id }) { localItem ->
                                LocalGridItem(
                                    localItem = localItem,
                                    navController = navController,
                                    playerConnection = playerConnection,
                                    mediaMetadata = mediaMetadata,
                                    isPlaying = isPlaying,
                                    coroutineScope = scope,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!accountPlaylists.isNullOrEmpty()) {
                    item(key = "ap_title") {
                        NavigationTitle(title = mixesTitle)
                    }
                    item(key = "ap_content") {
                        LazyRow(
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        ) {
                            items(accountPlaylists!!.take(10), key = { it.id }) { playlist ->
                                YouTubeGridItem(
                                    item = playlist,
                                    isActive = playlist.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                    isPlaying = isPlaying,
                                    thumbnailRatio = 1f,
                                    modifier = Modifier
                                        .width(160.dp)
                                        .clickable {
                                            navController.navigate("online_playlist/${playlist.id}")
                                        },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!forgottenFavorites.isNullOrEmpty()) {
                    val ffSongs = forgottenFavorites!!
                    item(key = "ff_title") {
                        NavigationTitle(
                            title = forgottenFavoritesTitle,
                            onPlayAllClick = {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = forgottenFavoritesTitle,
                                        items = ffSongs.distinctBy { it.id }.map { it.toMediaItem() },
                                    ),
                                )
                            },
                        )
                    }
                    item(key = "ff_content") {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(minOf(4, ffSongs.size)),
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * minOf(4, ffSongs.size)),
                        ) {
                            items(
                                items = ffSongs.distinctBy { it.id },
                                key = { "ff_${it.id}" },
                            ) { song ->
                                SongListItem(
                                    song = song,
                                    isActive = song.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    isSwipeable = false,
                                    modifier = Modifier
                                        .width(240.dp)
                                        .combinedClickable(
                                            onClick = {
                                                playerConnection.playQueue(
                                                    YouTubeQueue.radio(song.toMediaMetadata()),
                                                )
                                            },
                                        ),
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!communityPlaylists.isNullOrEmpty()) {
                    item(key = "cm_title") {
                        NavigationTitle(title = communityTitle)
                    }
                    item(key = "cm_content") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(communityPlaylists!!.take(10), key = { it.playlist.id }) { ci ->
                                CommunityPlaylistCard(
                                    item = ci,
                                    onClick = { navController.navigate("online_playlist/${ci.playlist.id}") },
                                    onSongClick = { song ->
                                        playerConnection.playQueue(
                                            YouTubeQueue(
                                                song.endpoint ?: WatchEndpoint(videoId = song.id),
                                                song.toMediaMetadata(),
                                            ),
                                        )
                                    },
                                    modifier = Modifier,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                similarRecommendations?.forEachIndexed { idx, rec ->
                    val title = rec.title
                    if (title != null && !rec.items.isNullOrEmpty()) {
                        item(key = "sr_title_$idx") {
                            NavigationTitle(title = "Similar to ${title.title}")
                        }
                        item(key = "sr_content_$idx") {
                            LazyRow(
                                contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                            ) {
                                items(rec.items!!.take(8), key = { it.id }) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                        isPlaying = isPlaying,
                                        thumbnailRatio = 1f,
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clickable {
                                                when (item) {
                                                    is SongItem -> playerConnection.playQueue(
                                                        YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()),
                                                    )
                                                    is AlbumItem -> navController.navigate("album/${item.id}")
                                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                    else -> {}
                                                }
                                            },
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                homePage?.sections?.forEachIndexed { idx, sec ->
                    val secItems = sec.items?.take(15) ?: emptyList()
                    val secTitle = sec.title
                    if (secItems.isNotEmpty() && secTitle != null) {
                        val isSongsOnly = secItems.all { it is SongItem }
                        item(key = "hp_title_$idx") {
                            NavigationTitle(title = secTitle)
                        }
                        item(key = "hp_content_$idx") {
                            if (isSongsOnly) {
                                LazyHorizontalGrid(
                                    rows = GridCells.Fixed(4),
                                    contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(ListItemHeight * 4),
                                ) {
                                    items(secItems, key = { it.id }) { item ->
                                        YouTubeListItem(
                                            item = item,
                                            isActive = item.id == mediaMetadata?.id,
                                            isPlaying = isPlaying,
                                            isSwipeable = false,
                                            modifier = Modifier
                                                .width(240.dp)
                                                .combinedClickable(
                                                    onClick = {
                                                        if (item is SongItem) {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                                                    item.toMediaMetadata(),
                                                                ),
                                                            )
                                                        }
                                                    },
                                                ),
                                        )
                                    }
                                }
                            } else {
                                LazyRow(
                                    contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                ) {
                                    items(secItems, key = { it.id }) { item ->
                                        YouTubeGridItem(
                                            item = item,
                                            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
                                            isPlaying = isPlaying,
                                            thumbnailRatio = 1f,
                                            modifier = Modifier
                                                .width(160.dp)
                                                .clickable {
                                                    when (item) {
                                                        is SongItem -> playerConnection.playQueue(
                                                            YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()),
                                                        )
                                                        is AlbumItem -> navController.navigate("album/${item.id}")
                                                        is ArtistItem -> navController.navigate("artist/${item.id}")
                                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        else -> {}
                                                    }
                                                },
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                if (homeLoading) {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun LoiterAnimatedArt(
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "home_art")

    val driftA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftA",
    )
    val driftB by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftB",
    )
    val driftC by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftC",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseColor = accentColor

        fun drawBlob(cxRatio: Float, cyRatio: Float, radiusRatio: Float, alpha: Float) {
            val cx = size.width * cxRatio
            val cy = size.height * cyRatio
            val radius = size.width * radiusRatio
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = alpha),
                        baseColor.copy(alpha = 0f),
                    ),
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    radius = radius,
                ),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
            )
        }

        drawBlob(0.1f + driftA * 0.2f, 0.05f + driftA * 0.1f, 0.55f, 0.20f)
        drawBlob(0.75f + driftB * 0.2f, 0.2f + driftB * 0.15f, 0.6f, 0.16f)
        drawBlob(0.3f + driftC * 0.25f, 0.55f + driftC * 0.2f, 0.65f, 0.18f)
    }
}

@Composable
private fun SpeedDialSection(
    items: List<YTItem>,
    navController: NavController,
    playerConnection: com.metrolist.music.playback.PlayerConnection,
) {
    val columns = 3
    val rows = 2
    val itemsPerPage = columns * rows
    val itemWidth = 120.dp

    val pagerState = rememberPagerState(pageCount = { (items.size + itemsPerPage - 1) / itemsPerPage })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemWidth * rows),
        ) { page ->
            val pageStartIndex = page * itemsPerPage
            val pageItems = items.drop(pageStartIndex).take(itemsPerPage)

            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until columns) {
                            val itemIndex = row * columns + col

                            if (itemIndex < pageItems.size) {
                                val item = pageItems[itemIndex]
                                Box(
                                    modifier = Modifier
                                        .width(itemWidth)
                                        .height(itemWidth)
                                        .padding(4.dp),
                                ) {
                                    SpeedDialGridItem(
                                        item = item,
                                        isPinned = false,
                                        isActive = false,
                                        isPlaying = false,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                when (item) {
                                                    is SongItem -> playerConnection.playQueue(
                                                        YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()),
                                                    )
                                                    is AlbumItem -> navController.navigate("album/${item.id}")
                                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                    else -> {}
                                                }
                                            },
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(itemWidth))
                            }
                        }
                    }
                }
            }
        }

        if (pagerState.pageCount > 1) {
            Row(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalGridItem(
    localItem: LocalItem,
    navController: NavController,
    playerConnection: com.metrolist.music.playback.PlayerConnection,
    mediaMetadata: com.metrolist.music.models.MediaMetadata?,
    isPlaying: Boolean,
    coroutineScope: CoroutineScope,
) {
    when (localItem) {
        is Song -> {
            val song = localItem
            SongGridItem(
                song = song,
                isActive = song.id == mediaMetadata?.id,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (song.id == mediaMetadata?.id) {
                            playerConnection.togglePlayPause()
                        } else {
                            playerConnection.playQueue(
                                YouTubeQueue.radio(song.toMediaMetadata()),
                            )
                        }
                    },
            )
        }
        is com.metrolist.music.db.entities.Album -> {
            AlbumGridItem(
                album = localItem,
                isActive = localItem.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                coroutineScope = coroutineScope,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("album/${localItem.id}") },
            )
        }
        is com.metrolist.music.db.entities.Artist -> {
            ArtistGridItem(
                artist = localItem,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("artist/${localItem.id}") },
            )
        }
        else -> {}
    }
}
