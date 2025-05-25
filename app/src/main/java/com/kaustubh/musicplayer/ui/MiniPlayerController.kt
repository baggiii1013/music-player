package com.kaustubh.musicplayer.ui

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.player.MusicPlayerManager

class MiniPlayerController(private val view: View, private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    
    private val albumArt: ImageView = view.findViewById(R.id.mini_album_art)
    private val songTitle: TextView = view.findViewById(R.id.mini_song_title)
    private val artistName: TextView = view.findViewById(R.id.mini_artist_name)
    private val playPauseButton: ImageButton = view.findViewById(R.id.mini_play_pause)
    private val nextButton: ImageButton = view.findViewById(R.id.mini_next)
    
    private val musicPlayer = MusicPlayerManager.getInstance(context)
    
    init {
        setupClickListeners()
        observePlaybackState()
    }
    
    private fun setupClickListeners() {
        playPauseButton.setOnClickListener {
            musicPlayer.playPause()
        }
        
        nextButton.setOnClickListener {
            // TODO: Implement next song functionality
        }
          view.setOnClickListener {
            FullPlayerActivity.start(context)
        }
    }
    
    private fun observePlaybackState() {
        musicPlayer.isPlaying.observe(lifecycleOwner) { isPlaying ->
            updatePlayPauseButton(isPlaying)
        }
        
        musicPlayer.currentSongLiveData.observe(lifecycleOwner) { song ->
            updateSong(song)
        }
    }
    
    fun updateSong(song: Song?) {
        song?.let {
            songTitle.text = it.title
            artistName.text = it.artist
            // TODO: Load album art from file
            albumArt.setImageResource(R.drawable.default_album_art)
            view.visibility = View.VISIBLE
        } ?: run {
            view.visibility = View.GONE
        }
    }
      private fun updatePlayPauseButton(isPlaying: Boolean) {
        val iconRes = if (isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play_arrow
        }
        playPauseButton.setImageResource(iconRes)
    }
    
    fun show() {
        view.visibility = View.VISIBLE
    }
    
    fun hide() {
        view.visibility = View.GONE
    }
}
