package com.metrolist.music.utils

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class ParsedSong(
    val title: String,
    val artist: String?,
    val album: String?,
)

data class ParsedPlaylist(
    val name: String,
    val songs: List<ParsedSong>,
)

data class ParsedFile(
    val playlists: List<ParsedPlaylist>,
)

object PlaylistFileImporter {
    fun parseFile(context: Context, uri: Uri): Result<ParsedFile> = runCatching {
        val content = readTextFile(context, uri)
        val trimmed = content.trim()
        when {
            trimmed.startsWith("[") || trimmed.startsWith("{\"") || trimmed.startsWith("[\"") ->
                parseJson(trimmed)
            trimmed.contains(",") || trimmed.startsWith("\"") || trimmed.firstOrNull()?.isLetterOrDigit() == true ->
                parseCsv(trimmed)
            else -> throw IllegalArgumentException("Unknown file format")
        }
    }

    private fun readTextFile(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        } ?: throw IllegalArgumentException("Could not read file")
    }

    private fun parseCsv(content: String): ParsedFile {
        val rawLines = content.lines()
        val lines = rawLines.map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) throw IllegalArgumentException("File is empty")

        val rows = lines.map { parseCsvLine(it) }
        val headerIndex = findHeaderIndex(rows)

        // Try to find playlist name from lines before the header (fallback)
        val fallbackPlaylistName = findPlaylistName(rawLines, headerIndex)

        val dataRows = if (headerIndex >= 0) {
            rows.drop(headerIndex + 1)
        } else {
            rows
        }

        // Detect column mapping from header
        val playlistCol: Int
        val titleCol: Int
        val artistCol: Int
        val albumCol: Int

        if (headerIndex >= 0) {
            val header = rows[headerIndex].map { it.trim().lowercase() }
            playlistCol = header.indexOfFirst { it in setOf("playlist", "playlist name", "playlist_name") }
            titleCol = header.indexOfFirst { it in setOf("name", "title", "track", "song") }
            artistCol = header.indexOfFirst { it in setOf("artist", "artists") }
            albumCol = header.indexOfFirst { it in setOf("album", "album name") }
        } else {
            playlistCol = -1
            titleCol = 0
            artistCol = 1
            albumCol = 2
        }

        // Map each row to a ParsedSong with optional playlist name, then group
        val songWithPlaylist = dataRows.mapNotNull { row ->
            val title = row.getOrNull(titleCol.takeIf { it >= 0 } ?: 0)?.trim()?.removeSurrounding("\"") ?: ""
            if (title.isBlank()) return@mapNotNull null
            val artist = row.getOrNull(artistCol.takeIf { it >= 0 } ?: 1)?.trim()?.removeSurrounding("\"")
            val album = row.getOrNull(albumCol.takeIf { it >= 0 } ?: 2)?.trim()?.removeSurrounding("\"")
            val playlist = if (playlistCol >= 0) {
                row.getOrNull(playlistCol)?.trim()?.removeSurrounding("\"")?.takeIf { it.isNotBlank() }
            } else null
            playlist to ParsedSong(title = title, artist = artist, album = album)
        }

        // Group by playlist name
        val grouped = songWithPlaylist.groupBy({ it.first }, { it.second })
        val playlists = if (grouped.size == 1 && grouped.keys.first() == null) {
            // Single playlist with no name column — use fallback name
            listOf(
                ParsedPlaylist(
                    name = fallbackPlaylistName ?: "Imported from file",
                    songs = grouped.values.first(),
                )
            )
        } else {
            grouped.map { (name, songs) ->
                ParsedPlaylist(
                    name = name ?: "Imported from file",
                    songs = songs,
                )
            }
        }

        return ParsedFile(playlists = playlists)
    }

    private fun findPlaylistName(rawLines: List<String>, headerIndex: Int): String? {
        val preHeaderLines = if (headerIndex > 0) {
            rawLines.take(headerIndex).map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        // Look for lines that start with common playlist name indicators
        for (line in preHeaderLines) {
            val lower = line.lowercase()
            if (lower.startsWith("#")) {
                // Comment line - might contain playlist name
                val cleaned = line.removePrefix("#").trim()
                val nameMatch = Regex("playlist[=: ]+(.+)", RegexOption.IGNORE_CASE).find(cleaned)
                if (nameMatch != null) return nameMatch.groupValues[1].trim().removeSurrounding("\"")
                // Otherwise return the comment text itself if it looks like a name
                if (cleaned.isNotBlank()) return cleaned
            }
            if (lower.startsWith("playlist")) {
                val nameMatch = Regex("playlist[=: ]+(.+)", RegexOption.IGNORE_CASE).find(line)
                if (nameMatch != null) return nameMatch.groupValues[1].trim().removeSurrounding("\"")
            }
        }

        // If there's a single non-header non-data line before header, use it as the name
        val singleLines = preHeaderLines.filter { 
            !it.startsWith("#") && 
            !it.lowercase().startsWith("playlist") &&
            parseCsvLine(it).size <= 2
        }
        if (singleLines.size == 1) {
            val parts = parseCsvLine(singleLines[0])
            if (parts.size <= 2 && parts[0].isNotBlank()) {
                return parts[0].trim().removeSurrounding("\"")
            }
        }

        return null
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }

    private fun findHeaderIndex(rows: List<List<String>>): Int {
        val headerKeywords = setOf("name", "title", "track", "artist", "artists", "album")
        for ((index, row) in rows.withIndex()) {
            val lowerRow = row.map { it.trim().lowercase() }
            if (lowerRow.any { it in headerKeywords }) {
                return index
            }
        }
        return -1
    }

    private fun parseJson(content: String): ParsedFile {
        val arr = if (content.trimStart().startsWith("[")) {
            JSONArray(content)
        } else {
            JSONArray("[$content]")
        }

        // Try to find playlist name from JSON (fallback)
        val fallbackPlaylistName = findJsonPlaylistName(arr)

        // Parse each object, extracting optional playlist name per song
        val songWithPlaylist = (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            val song = parseJsonObject(obj)
            if (song.title.isBlank()) return@mapNotNull null
            val playlistName = obj.optString("playlistName", "")
                .takeIf { it.isNotBlank() }
                ?: obj.optString("playlist_name", "")
                .takeIf { it.isNotBlank() }
                ?: obj.optString("playlist", "")
                .takeIf { it.isNotBlank() }
            playlistName to song
        }

        // Group by playlist name
        val grouped = songWithPlaylist.groupBy({ it.first }, { it.second })
        val playlists = if (grouped.size == 1 && grouped.keys.first() == null) {
            listOf(
                ParsedPlaylist(
                    name = fallbackPlaylistName ?: "Imported from file",
                    songs = grouped.values.first(),
                )
            )
        } else {
            grouped.map { (name, songs) ->
                ParsedPlaylist(
                    name = name ?: "Imported from file",
                    songs = songs,
                )
            }
        }

        return ParsedFile(playlists = playlists)
    }

    private fun findJsonPlaylistName(arr: JSONArray): String? {
        // Check if the first object has a playlist-level name field
        val first = arr.optJSONObject(0) ?: return null
        return first.optString("playlistName", "")
            .takeIf { it.isNotBlank() }
            ?: first.optString("playlist_name", "")
            .takeIf { it.isNotBlank() }
            ?: first.optString("name", "")
            .takeIf { arr.length() == 1 && it.isNotBlank() }
    }

    private fun parseJsonObject(obj: JSONObject): ParsedSong {
        val title = obj.optString("name", "")
            .takeIf { it.isNotBlank() }
            ?: obj.optString("title", "")
            ?: obj.optString("track", "")
            ?: ""

        val artist = obj.optString("artist", "")
            .takeIf { it.isNotBlank() }
            ?: obj.optString("artists", "")
            .takeIf { it.isNotBlank() }
            ?.let { parseJsonArtist(it) }
            ?: obj.optJSONArray("artists")?.let { parseJsonArtistArray(it) }

        val album = obj.optString("album", "")
            .takeIf { it.isNotBlank() }
            ?: obj.optJSONObject("album")?.optString("name", "")

        return ParsedSong(title = title, artist = artist, album = album)
    }

    private fun parseJsonArtist(artistStr: String): String {
        return try {
            val arr = JSONArray(artistStr)
            (0 until arr.length()).joinToString(", ") { arr.optString(it, "") }
        } catch (_: Exception) {
            artistStr
        }
    }

    private fun parseJsonArtistArray(arr: JSONArray): String {
        return (0 until arr.length()).joinToString(", ") { i ->
            val obj = arr.optJSONObject(i)
            if (obj != null) obj.optString("name", "") else arr.optString(i, "")
        }
    }
}
