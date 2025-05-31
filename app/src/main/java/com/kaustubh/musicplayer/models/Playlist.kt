package com.kaustubh.musicplayer.models

import java.util.Date

data class Playlist(
    val id: Long = System.currentTimeMillis(), // Simple ID generation
    val name: String,
    val description: String = "",
    val createdDate: Date = Date(),
    val songs: MutableList<Song> = mutableListOf()
) {
    fun getSongCount(): Int = songs.size
    
    fun getDurationString(): String {
        val totalDuration = songs.sumOf { it.duration }
        val minutes = totalDuration / 1000 / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        
        return if (hours > 0) {
            String.format("%d:%02d", hours, remainingMinutes)
        } else {
            String.format("%d min", minutes)
        }
    }
    
    fun addSong(song: Song) {
        if (!songs.contains(song)) {
            songs.add(song)
        }
    }
    
    fun removeSong(song: Song) {
        songs.remove(song)
    }
    
    fun containsSong(song: Song): Boolean {
        return songs.contains(song)
    }
}
