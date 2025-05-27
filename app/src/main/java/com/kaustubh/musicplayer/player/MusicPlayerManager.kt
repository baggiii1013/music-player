package com.kaustubh.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.service.MusicService

class MusicPlayerManager private constructor(private val context: Context) : MusicService.PlaybackStateCallback {
    
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
            // Register callback to receive state changes from service
            musicService?.setPlaybackStateCallback(this@MusicPlayerManager)
            
            // Sync current playlist with service if we have one
            if (currentPlaylist.isNotEmpty() && currentSong != null) {
                Log.d("MusicPlayerManager", "Service connected, syncing current playlist")
                musicService?.setPlaylist(currentPlaylist)
            }
            
            Log.d("MusicPlayerManager", "Service connected successfully")
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("MusicPlayerManager", "Service disconnected")
            musicService?.setPlaybackStateCallback(null)
            musicService = null
            isBound = false
        }
    }
    
    init {
        // Bind to MusicService for MediaSession support
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
      fun playSong(song: Song, playlist: List<Song> = listOf(song)) {
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
                Log.d("MusicPlayerManager", "Playing song through service: ${song.title}, playlist size: ${playlist.size}")
                musicService!!.playSong(song, playlist)
            } else {
                Log.d("MusicPlayerManager", "Service not available, using local MediaPlayer")
                // Fallback to local MediaPlayer if service not available
                playLocalMediaPlayer(song)
            }
        } catch (e: Exception) {
            Log.e("MusicPlayerManager", "Error playing song: ${e.message}", e)
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
    }    fun nextSong() {
        Log.d("MusicPlayerManager", "nextSong() called - isBound: $isBound, service available: ${musicService != null}")
        Log.d("MusicPlayerManager", "Current playlist size: ${currentPlaylist.size}, current index: $currentSongIndex")
        
        if (currentPlaylist.isEmpty()) {
            Log.w("MusicPlayerManager", "nextSong() called but currentPlaylist is empty")
            return
        }
        
        if (isBound && musicService != null) {
            // Use service for next song to maintain MediaSession state
            Log.d("MusicPlayerManager", "Calling service nextSong()")
            try {
                musicService!!.nextSong()
                // Let the callback handle state updates
                Log.d("MusicPlayerManager", "Service nextSong() call completed")
            } catch (e: Exception) {
                Log.e("MusicPlayerManager", "Error calling service nextSong: ${e.message}", e)
                // Fallback to local logic if service fails
                executeLocalNextSong()
            }
        } else {
            // Fallback to local logic
            Log.d("MusicPlayerManager", "Service not available, using fallback logic for nextSong()")
            executeLocalNextSong()
        }
    }
    
    private fun executeLocalNextSong() {
        if (currentPlaylist.isNotEmpty()) {
            val oldIndex = currentSongIndex
            if (isShuffleEnabled) {
                // If shuffle is on, play a random song
                currentSongIndex = (0 until currentPlaylist.size).random()
                Log.d("MusicPlayerManager", "Shuffle mode: playing random song at index $currentSongIndex")
            } else if (currentSongIndex < currentPlaylist.size - 1) {
                // Move to next song
                currentSongIndex++
                Log.d("MusicPlayerManager", "Moving to next song at index $currentSongIndex")
            } else {
                // At the end of playlist, loop back to beginning
                currentSongIndex = 0
                Log.d("MusicPlayerManager", "Looping to beginning: playing song at index $currentSongIndex")
            }
            
            if (oldIndex != currentSongIndex && currentSongIndex < currentPlaylist.size) {
                playSong(currentPlaylist[currentSongIndex], currentPlaylist)
            }
        }
    }    fun previousSong() {
        Log.d("MusicPlayerManager", "previousSong() called - isBound: $isBound, service available: ${musicService != null}")
        Log.d("MusicPlayerManager", "Current playlist size: ${currentPlaylist.size}, current index: $currentSongIndex")
        
        if (currentPlaylist.isEmpty()) {
            Log.w("MusicPlayerManager", "previousSong() called but currentPlaylist is empty")
            return
        }
        
        if (isBound && musicService != null) {
            // Use service for previous song to maintain MediaSession state
            Log.d("MusicPlayerManager", "Calling service previousSong()")
            try {
                musicService!!.previousSong()
                // Let the callback handle state updates
                Log.d("MusicPlayerManager", "Service previousSong() call completed")
            } catch (e: Exception) {
                Log.e("MusicPlayerManager", "Error calling service previousSong: ${e.message}", e)
                // Fallback to local logic if service fails
                executeLocalPreviousSong()
            }
        } else {
            // Fallback to local logic
            Log.d("MusicPlayerManager", "Service not available, using fallback logic for previousSong()")
            executeLocalPreviousSong()
        }
    }
    
    private fun executeLocalPreviousSong() {
        if (currentPlaylist.isNotEmpty()) {
            val oldIndex = currentSongIndex
            if (isShuffleEnabled) {
                // If shuffle is on, play a random song
                currentSongIndex = (0 until currentPlaylist.size).random()
                Log.d("MusicPlayerManager", "Shuffle mode: playing random song at index $currentSongIndex")
            } else if (currentSongIndex > 0) {
                // Move to previous song
                currentSongIndex--
                Log.d("MusicPlayerManager", "Moving to previous song at index $currentSongIndex")
            } else {
                // At the beginning of playlist, loop to end
                currentSongIndex = currentPlaylist.size - 1
                Log.d("MusicPlayerManager", "Looping to end: playing song at index $currentSongIndex")
            }
            
            if (oldIndex != currentSongIndex && currentSongIndex < currentPlaylist.size) {
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
      // Implement MusicService.PlaybackStateCallback
    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        Log.d("MusicPlayerManager", "Playback state changed: $isPlaying")
        _isPlaying.value = isPlaying
    }
    
    override fun onSongChanged(song: Song?) {
        Log.d("MusicPlayerManager", "Song changed: ${song?.title}")
        currentSong = song
        _currentSongLiveData.value = song
        if (song != null && currentPlaylist.isNotEmpty()) {
            val newIndex = currentPlaylist.indexOf(song)
            if (newIndex != -1) {
                currentSongIndex = newIndex
                Log.d("MusicPlayerManager", "Updated currentSongIndex to: $currentSongIndex")
            }
        }
    }
}
