package com.metrolist.music.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.utils.ParsedFile
import com.metrolist.music.utils.PlaylistFileImporter
import com.metrolist.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlaylistImportViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
) : ViewModel() {
    private val _importProgress = MutableStateFlow<ImportProgress>(ImportProgress.Idle)
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()

    private val _fileImportState = MutableStateFlow<FileImportState>(FileImportState.Idle)
    val fileImportState: StateFlow<FileImportState> = _fileImportState.asStateFlow()

    sealed class ImportProgress {
        data object Idle : ImportProgress()
        data class InProgress(val current: Int, val total: Int, val playlistName: String) : ImportProgress()
        data class Done(val message: String) : ImportProgress()
        data class Error(val message: String) : ImportProgress()
    }

    sealed class FileImportState {
        data object Idle : FileImportState()
        data class FileSelected(val parsed: ParsedFile) : FileImportState()
        data class Importing(val current: Int, val total: Int) : FileImportState()
        data object Done : FileImportState()
        data class Error(val message: String) : FileImportState()
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parsed = PlaylistFileImporter.parseFile(context, uri).getOrThrow()
                val totalSongs = parsed.playlists.sumOf { it.songs.size }
                if (totalSongs == 0) {
                    _fileImportState.value = FileImportState.Error(
                        context.getString(com.metrolist.music.R.string.playlist_import_file_no_songs)
                    )
                } else {
                    _fileImportState.value = FileImportState.FileSelected(parsed)
                }
            } catch (e: Exception) {
                _fileImportState.value = FileImportState.Error(
                    e.message ?: context.getString(com.metrolist.music.R.string.playlist_import_file_invalid)
                )
            }
        }
    }

    fun importFileSongs(parsed: ParsedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalSongs = parsed.playlists.sumOf { it.songs.size }
            _fileImportState.value = FileImportState.Importing(current = 0, total = totalSongs)

            try {
                var processedSongs = 0
                var totalImported = 0

                for (playlistGroup in parsed.playlists) {
                    val newPlaylist = PlaylistEntity(
                        name = playlistGroup.name,
                        isLocal = true,
                        bookmarkedAt = LocalDateTime.now(),
                    )
                    database.insert(newPlaylist)

                    val savedPlaylist = database.playlist(newPlaylist.id).first { it != null }!!

                    val songIds = mutableListOf<Pair<String, String?>>()

                    for (song in playlistGroup.songs) {
                        val query = buildString {
                            append(song.title)
                            if (!song.artist.isNullOrBlank()) {
                                append(" ")
                                append(song.artist)
                            }
                        }

                        try {
                            val searchResult = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG)
                                .getOrNull()
                            val songItem = searchResult?.items
                                ?.filterIsInstance<SongItem>()
                                ?.firstOrNull()

                            if (songItem != null) {
                                database.insert(songItem.toMediaMetadata()) {
                                    it.copy(inLibrary = LocalDateTime.now())
                                }
                                songIds.add(songItem.id to null)
                            }
                        } catch (_: Exception) {
                        }

                        processedSongs++
                        _fileImportState.value = FileImportState.Importing(
                            current = processedSongs,
                            total = totalSongs,
                        )
                    }

                    if (songIds.isNotEmpty()) {
                        database.addSongsToPlaylist(savedPlaylist, songIds)
                    }

                    totalImported += songIds.size
                }

                _fileImportState.value = FileImportState.Done
            } catch (e: Exception) {
                _fileImportState.value = FileImportState.Error(
                    e.message ?: "Import failed"
                )
                reportException(e)
            }
        }
    }

    fun resetFileImport() {
        _fileImportState.value = FileImportState.Idle
    }

    fun dismissError() {
        val current = _importProgress.value
        if (current is ImportProgress.Error || current is ImportProgress.Done) {
            _importProgress.value = ImportProgress.Idle
        }
    }
}
