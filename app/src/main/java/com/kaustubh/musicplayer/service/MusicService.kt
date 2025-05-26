package com.kaustubh.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.kaustubh.musicplayer.MainActivity
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song

class MusicService : Service() {
      companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "music_playback_channel"
        private const val MEDIA_SESSION_TAG = "MusicPlayerMediaSession"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var currentPlaylist: List<Song> = emptyList()
    private var currentSongIndex: Int = -1
    private val binder = MusicBinder()
    
    // MediaSession for Android 15 Quick Settings integration
    private var mediaSession: MediaSessionCompat? = null
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
      override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupMediaSession()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
      override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        
        when (intent?.action) {
            "PLAY_PAUSE" -> playPause()
            "NEXT" -> playNext()
            "PREVIOUS" -> playPrevious()
            "STOP" -> stopSelf()
        }
        return START_STICKY
    }
      private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }
    }
    
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            resumeMusic()
        }
        
        override fun onPause() {
            pauseMusic()
        }
        
        override fun onSkipToNext() {
            nextSong()
        }
        
        override fun onSkipToPrevious() {
            previousSong()
        }
        
        override fun onStop() {
            stopSelf()
        }
          override fun onSeekTo(pos: Long) {
            seekTo(pos.toInt())
        }
    }
    
    fun playSong(song: Song, playlist: List<Song> = listOf(song)) {
        try {
            mediaPlayer?.release()
            
            currentPlaylist = playlist
            currentSongIndex = playlist.indexOf(song)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, song.uri)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    currentSong = song
                    updateMediaSessionMetadata(song)
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    startForeground(NOTIFICATION_ID, createNotification())
                }
                setOnCompletionListener {
                    if (isRepeatEnabled) {
                        // Repeat current song
                        seekTo(0)
                        start()
                        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    } else {
                        playNext()
                    }
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
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            } else {
                player.start()
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }
            updateNotification()
        }
    }fun nextSong() {
        if (currentPlaylist.isNotEmpty() && currentSongIndex < currentPlaylist.size - 1) {
            currentSongIndex++
            playSong(currentPlaylist[currentSongIndex], currentPlaylist)
        } else if (isShuffleEnabled && currentPlaylist.isNotEmpty()) {
            // If shuffle is on, play a random song
            currentSongIndex = (0 until currentPlaylist.size).random()
            playSong(currentPlaylist[currentSongIndex], currentPlaylist)
        }
    }
    
    fun previousSong() {
        if (currentPlaylist.isNotEmpty() && currentSongIndex > 0) {
            currentSongIndex--
            playSong(currentPlaylist[currentSongIndex], currentPlaylist)
        } else if (isShuffleEnabled && currentPlaylist.isNotEmpty()) {
            // If shuffle is on, play a random song
            currentSongIndex = (0 until currentPlaylist.size).random()
            playSong(currentPlaylist[currentSongIndex], currentPlaylist)
        }
    }
    
    fun playNext() {
        nextSong()
    }
    
    fun playPrevious() {
        previousSong()
    }
      fun pauseMusic() {
        mediaPlayer?.pause()
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        updateNotification()
    }
      fun resumeMusic() {
        mediaPlayer?.start()
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        updateNotification()
    }
    
    fun stop() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
        currentSong = null
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        stopForeground(true)
    }
    
    fun getCurrentSong(): Song? {
        return currentSong
    }
    
    private var isShuffleEnabled = false
    private var isRepeatEnabled = false
      fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }
    
    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
    }
    
    fun setShuffleEnabled(enabled: Boolean) {
        isShuffleEnabled = enabled
    }
    
    fun setRepeatEnabled(enabled: Boolean) {
        isRepeatEnabled = enabled
    }
    
    fun isShuffleEnabled(): Boolean {
        return isShuffleEnabled
    }
    
    fun isRepeatEnabled(): Boolean {
        return isRepeatEnabled
    }
    
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
      fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        updatePlaybackState(
            if (isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        )
    }
    
    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }
    
    fun setPlaylist(playlist: List<Song>) {
        currentPlaylist = playlist
    }
    
    fun getCurrentPlaylist(): List<Song> {
        return currentPlaylist
    }
    
    private fun updateMediaSessionMetadata(song: Song) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .build()
        
        mediaSession?.setMetadata(metadata)
    }
    
    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(state, getCurrentPosition().toLong(), 1.0f)
            .build()
        
        mediaSession?.setPlaybackState(playbackState)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
      private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val playPauseAction = if (isPlaying()) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                getString(R.string.pause),
                createPendingIntent("PLAY_PAUSE")
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play_arrow,
                getString(R.string.play),
                createPendingIntent("PLAY_PAUSE")
            )
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentSong?.title ?: getString(R.string.no_song_playing))
            .setContentText(currentSong?.artist ?: getString(R.string.unknown_artist))
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_skip_previous,
                getString(R.string.previous),
                createPendingIntent("PREVIOUS")
            )
            .addAction(playPauseAction)
            .addAction(
                R.drawable.ic_skip_next,
                getString(R.string.next),
                createPendingIntent("NEXT")
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession?.sessionToken)
            )
            .setOnlyAlertOnce(true)
            .build()
    }
    
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
      override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession?.release()
        mediaSession = null
    }
}
