package com.kaustubh.musicplayer.models

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val uri: Uri
) {
    fun getDurationString(): String {
        val minutes = duration / 1000 / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
