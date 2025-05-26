package com.kaustubh.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.service.MusicService

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
    
    private var musicService: MusicService? = null
    private var isBound = false
    
    // Keep local MediaPlayer for immediate UI updates while also using service
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private val _isPlaying = MutableLiveData<Boolean>(false)
    private val _currentPosition = MutableLiveData<Int>(0)
    private val _currentSongLiveData = MutableLiveData<Song?>()
    private val _allSongsLiveData = MutableLiveData<List<Song>>(emptyList())
      val isPlaying: LiveData<Boolean> = _isPlaying
    val currentPosition: LiveData<Int> = _currentPosition
    val currentSongLiveData: LiveData<Song?> = _currentSongLiveData
    val allSongsLiveData: LiveData<List<Song>> = _allSongsLiveData
    
    private var isShuffleEnabled = false
    private var isRepeatEnabled = false
    private var currentPlaylist: List<Song> = emptyList()
    private var currentSongIndex: Int = -1
    
    // Service connection for MusicService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }
      init {
        // Bind to MusicService for MediaSession support
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }    fun playSong(song: Song, playlist: List<Song> = listOf(song)) {
        try {
            // Start the service to ensure it's running for Quick Settings
            val serviceIntent = Intent(context, MusicService::class.java)
            context.startService(serviceIntent)
            
            // Update local state immediately for UI responsiveness
            currentPlaylist = playlist
            currentSongIndex = playlist.indexOf(song)
            currentSong = song
            _currentSongLiveData.value = song
            
            // Also play through service for MediaSession support
            if (isBound && musicService != null) {
                musicService!!.playSong(song, playlist)
            } else {
                // Fallback to local MediaPlayer if service not available
                playLocalMediaPlayer(song)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun playLocalMediaPlayer(song: Song) {
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
                }
                setOnCompletionListener {
                    if (isRepeatEnabled) {
                        // Repeat current song
                        seekTo(0)
                        start()
                    } else {
                        nextSong()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
      fun playPause() {
        // Try service first, fallback to local MediaPlayer
        if (isBound && musicService != null) {
            musicService!!.playPause()
            _isPlaying.value = musicService!!.isPlaying()
        } else {
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
    }
      fun stop() {
        // Stop both service and local MediaPlayer
        if (isBound && musicService != null) {
            musicService!!.stop()
        }
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            _isPlaying.value = false
            _currentPosition.value = 0
        }
    }
      fun seekTo(position: Int) {
        // Seek on both service and local MediaPlayer
        if (isBound && musicService != null) {
            musicService!!.seekTo(position)
        }
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }
      fun getCurrentPosition(): Int {
        return if (isBound && musicService != null) {
            musicService!!.getCurrentPosition()
        } else {
            mediaPlayer?.currentPosition ?: 0
        }
    }
    
    fun getDuration(): Int {
        return if (isBound && musicService != null) {
            musicService!!.getDuration()
        } else {
            mediaPlayer?.duration ?: 0
        }
    }
    
    fun getAudioSessionId(): Int {
        return if (isBound && musicService != null) {
            musicService!!.getAudioSessionId()
        } else {
            mediaPlayer?.audioSessionId ?: 0
        }
    }
    
    fun getCurrentSong(): Song? {
        return currentSong
    }
    
    fun updateAllSongs(songs: List<Song>) {
        _allSongsLiveData.value = songs
    }
      fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        // Sync with service if available
        if (isBound && musicService != null) {
            musicService!!.setShuffleEnabled(isShuffleEnabled)
        }
    }
    
    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
        // Sync with service if available
        if (isBound && musicService != null) {
            musicService!!.setRepeatEnabled(isRepeatEnabled)
        }
    }
    
    fun isShuffleEnabled(): Boolean {
        return isShuffleEnabled
    }
    
    fun isRepeatEnabled(): Boolean {
        return isRepeatEnabled
    }
    
    fun setPlaylist(playlist: List<Song>) {
        currentPlaylist = playlist
    }
      fun nextSong() {
        if (isBound && musicService != null) {
            // Use service for next song to maintain MediaSession state
            musicService!!.nextSong()
            // Update local state to match
            currentSong = musicService!!.getCurrentSong()
            _currentSongLiveData.value = currentSong
            _isPlaying.value = musicService!!.isPlaying()
        } else {
            // Fallback to local logic
            if (currentPlaylist.isNotEmpty() && currentSongIndex < currentPlaylist.size - 1) {
                currentSongIndex++
                playSong(currentPlaylist[currentSongIndex], currentPlaylist)
            } else if (isShuffleEnabled && currentPlaylist.isNotEmpty()) {
                currentSongIndex = (0 until currentPlaylist.size).random()
                playSong(currentPlaylist[currentSongIndex], currentPlaylist)
            }
        }
    }
      fun previousSong() {
        if (isBound && musicService != null) {
            // Use service for previous song to maintain MediaSession state
            musicService!!.previousSong()
            // Update local state to match
            currentSong = musicService!!.getCurrentSong()
            _currentSongLiveData.value = currentSong
            _isPlaying.value = musicService!!.isPlaying()
        } else {
            // Fallback to local logic
            if (currentPlaylist.isNotEmpty() && currentSongIndex > 0) {
                currentSongIndex--
                playSong(currentPlaylist[currentSongIndex], currentPlaylist)
            } else if (isShuffleEnabled && currentPlaylist.isNotEmpty()) {
                currentSongIndex = (0 until currentPlaylist.size).random()
                playSong(currentPlaylist[currentSongIndex], currentPlaylist)
            }
        }
    }
      fun isServiceConnected(): Boolean {
        return isBound && musicService != null
    }
    
    fun getServiceStatus(): String {
        return when {
            isBound && musicService != null -> "Service connected and available"
            isBound && musicService == null -> "Service bound but not ready"
            else -> "Service not connected"
        }
    }
    
    fun release() {
        // Unbind from service
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
                isBound = false
                musicService = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Release local MediaPlayer
        mediaPlayer?.release()
        mediaPlayer = null
        currentSong = null
        _isPlaying.value = false
        _currentPosition.value = 0
        _currentSongLiveData.value = null
    }
}
