package com.example.playlistmaker

// "Мок" (mock) — заглушка вместо настоящих сервера и базы данных.
// Все данные лежат в памяти и сбрасываются при перезапуске приложения.
// object = синглтон: один общий экземпляр на всё приложение.
object MockRepository {

    private const val HISTORY_LIMIT = 10

    private val tracks = mutableListOf(
        Track(1, "Ain't in LA", "ADELA", 222, "PRIMA", 2026, "Поп", "Словакия"),
        Track(2, "Still loving you", "Scorpions", 245, "From the first sting", 2025, "Рок", "Великобритания"),
        Track(3, "Put you on the game", "The Game", 198, "The Documentary", 2005, "Рэп", "США"),
        Track(4, "Black Star", "Radiohead", 213, "The Bends", 1995, "Рок", "Великобритания"),
        Track(5, "Too Good to Say Goodbye", "Bruno Mars", 187, "24K Magic", 2016, "Поп", "CША"),
        Track(6, "Lithium", "Nirvana", 204, "Nirvana", 2002, "Рок", "США"),
        Track(7, "Outside", "The Weeknd", 256, "Trilogy", 2012, "Поп", "США"),
        Track(8, "Why", "Limp Bizkit", 231, "Greatest Hitz", 2005, "Рок", "США"),
        Track(9, "Trouble", "Coldplay", 301, "Parachutes", 2000, "Поп", "США"),
        Track(10, "Nicole Kidman", "ADELA", 279, "PRIMA", 2026, "Поп", "Словакия"),
        Track(11, "Часики", "Валерия", 168, "Это время любви (Лучшее о любви)", 2015, "Поп", "Россия"),
        Track(12, "From The Start", "Laufey", 210, "Bewitched", 2023, "Блюз", "Франция")
    )

    private val favoriteIds = mutableListOf<Int>()   // новые — в начале
    private val historyIds = mutableListOf<Int>()    // последние открытые — в начале
    private val playlists = mutableListOf<Playlist>()
    private var nextPlaylistId = 1

    // ---------- Треки ----------
    fun getTrack(id: Int): Track? = tracks.firstOrNull { it.id == id }

    fun search(query: String): List<Track> {
        val q = query.trim().lowercase()
        return tracks.filter { it.name.lowercase().contains(q) || it.artist.lowercase().contains(q) }
    }

    fun nextTrackId(currentId: Int): Int? {
        if (tracks.isEmpty()) return null
        val index = tracks.indexOfFirst { it.id == currentId }
        return tracks[(index + 1) % tracks.size].id
    }

    // УДАЛЕНИЕ ТРЕКА: убираем его отовсюду — из каталога, избранного, истории и всех плейлистов
    fun deleteTrack(id: Int) {
        tracks.removeAll { it.id == id }
        favoriteIds.remove(id)
        historyIds.remove(id)
        playlists.forEach { it.trackIds.remove(id) }
    }

    // ---------- Избранное ----------
    fun getFavorites(): List<Track> = favoriteIds.mapNotNull { getTrack(it) }

    fun isFavorite(id: Int): Boolean = favoriteIds.contains(id)

    // true — трек добавили, false — убрали
    fun toggleFavorite(id: Int): Boolean {
        if (favoriteIds.remove(id)) return false
        favoriteIds.add(0, id)
        return true
    }

    // УДАЛЕНИЕ ИЗ ИЗБРАННОГО
    fun removeFromFavorites(id: Int) {
        favoriteIds.remove(id)
    }

    // ---------- История поиска ----------
    fun getHistory(): List<Track> = historyIds.mapNotNull { getTrack(it) }

    fun addToHistory(id: Int) {
        historyIds.remove(id)
        historyIds.add(0, id)
        while (historyIds.size > HISTORY_LIMIT) historyIds.removeAt(historyIds.lastIndex)
    }

    fun clearHistory() {
        historyIds.clear()
    }

    // ---------- Плейлисты ----------
    fun getPlaylists(): List<Playlist> = playlists.toList()

    fun getPlaylist(id: Int): Playlist? = playlists.firstOrNull { it.id == id }

    fun createPlaylist(name: String, description: String): Playlist {
        val playlist = Playlist(nextPlaylistId++, name.trim(), description.trim())
        playlists.add(playlist)
        return playlist
    }

    // УДАЛЕНИЕ САМОГО ПЛЕЙЛИСТА
    fun deletePlaylist(id: Int) {
        playlists.removeAll { it.id == id }
    }

    // false — трек уже был в плейлисте
    fun addTrackToPlaylist(playlistId: Int, trackId: Int): Boolean {
        val playlist = getPlaylist(playlistId) ?: return false
        if (playlist.trackIds.contains(trackId)) return false
        playlist.trackIds.add(trackId)
        return true
    }

    // УДАЛЕНИЕ ТРЕКА ИЗ ПЛЕЙЛИСТА
    fun removeTrackFromPlaylist(playlistId: Int, trackId: Int) {
        getPlaylist(playlistId)?.trackIds?.remove(trackId)
    }

    fun getPlaylistTracks(playlistId: Int): List<Track> =
        getPlaylist(playlistId)?.trackIds?.mapNotNull { getTrack(it) } ?: emptyList()
}
