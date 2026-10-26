package com.example.playlistmaker

data class Track(
    val id: Int,
    val name: String,
    val artist: String,
    val durationSec: Int,
    val album: String,
    val year: Int,
    val genre: String,
    val country: String
)

data class Playlist(
    val id: Int,
    var name: String,
    var description: String,
    val trackIds: MutableList<Int> = mutableListOf()
)
