package com.kaustubh.musicplayer.player

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kaustubh.musicplayer.models.Song

class MusicPlayerManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: MusicPlayerManager? = null
        
        fun getInstance(context: Context): MusicPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicPlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private val _isPlaying = MutableLiveData<Boolean>(false)
    private val _currentPosition = MutableLiveData<Int>(0)
    private val _currentSongLiveData = MutableLiveData<Song?>()
    
    val isPlaying: LiveData<Boolean> = _isPlaying
    val currentPosition: LiveData<Int> = _currentPosition
    val currentSongLiveData: LiveData<Song?> = _currentSongLiveData
    
    fun playSong(song: Song) {
        try {
            // Release previous MediaPlayer
            mediaPlayer?.release()
            
            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, song.uri)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    currentSong = song
                    _currentSongLiveData.value = song
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun playPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                _isPlaying.value = true
            }
        }
    }
    
    fun stop() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            _isPlaying.value = false
            _currentPosition.value = 0
        }
    }
    
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }
    
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
    
    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }
    
    fun getCurrentSong(): Song? {
        return currentSong
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentSong = null
        _isPlaying.value = false
        _currentPosition.value = 0
        _currentSongLiveData.value = null
    }
}
