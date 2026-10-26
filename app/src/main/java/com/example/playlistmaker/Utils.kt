package com.example.playlistmaker

import java.util.Locale

// 222 секунды -> "3:42"
fun formatDuration(totalSec: Int): String =
    String.format(Locale.getDefault(), "%d:%02d", totalSec / 60, totalSec % 60)

// 1 трек, 2 трека, 5 треков, 11 треков, 21 трек ...
fun tracksCountText(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    val word = if (mod100 in 11..19) {
        "треков"
    } else {
        when (mod10) {
            1 -> "трек"
            2, 3, 4 -> "трека"
            else -> "треков"
        }
    }
    return "$count $word"
}
