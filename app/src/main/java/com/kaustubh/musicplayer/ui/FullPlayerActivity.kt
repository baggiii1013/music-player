package com.kaustubh.musicplayer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.player.MusicPlayerManager
import com.kaustubh.musicplayer.utils.AlbumArtUtils

class FullPlayerActivity : AppCompatActivity() {
    
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var ivAlbumArt: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnShuffle: ImageButton
    private lateinit var btnRepeat: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnPlaylist: ImageButton
    
    private lateinit var musicPlayerManager: MusicPlayerManager
    
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateSeekBar()
            handler.postDelayed(this, 1000) // Update every second
        }
    }    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_player)
        
        musicPlayerManager = MusicPlayerManager.getInstance(this)
        
        initializeViews()
        setupClickListeners()
        setupObservers()
        updateUI()
        handler.post(updateRunnable) // Start progress updates
    }
    
    private fun initializeViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvArtistName = findViewById(R.id.tvArtistName)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        ivAlbumArt = findViewById(R.id.ivAlbumArt)
        seekBar = findViewById(R.id.seekBar)
        btnBack = findViewById(R.id.btnBack)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnShuffle = findViewById(R.id.btnShuffle)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnPlaylist = findViewById(R.id.btnPlaylist)    }
      private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnPlayPause.setOnClickListener {
            Log.d("FullPlayerActivity", "Play/Pause button clicked")
            musicPlayerManager.playPause()
        }
        
        btnNext.setOnClickListener {
            Log.d("FullPlayerActivity", "Next button clicked")
            Log.d("FullPlayerActivity", "Service status: ${musicPlayerManager.getServiceStatus()}")
            musicPlayerManager.nextSong()
        }
        
        btnPrevious.setOnClickListener {
            Log.d("FullPlayerActivity", "Previous button clicked")
            Log.d("FullPlayerActivity", "Service status: ${musicPlayerManager.getServiceStatus()}")
            musicPlayerManager.previousSong()
        }
        
        btnShuffle.setOnClickListener {
            musicPlayerManager.toggleShuffle()
            updateShuffleButton()
        }
        
        btnRepeat.setOnClickListener {
            musicPlayerManager.toggleRepeat()
            updateRepeatButton()
        }
        
        btnFavorite.setOnClickListener {
            // TODO: Implement favorite functionality
        }
        
        btnPlaylist.setOnClickListener {
            // TODO: Implement add to playlist functionality
        }
          seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayerManager.seekTo(progress)
                    updateCurrentTime(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
      private fun setupObservers() {
        musicPlayerManager.currentSongLiveData.observe(this, Observer { song ->
            Log.d("FullPlayerActivity", "Song changed observed: ${song?.title}")
            updateUI()
        })
        
        musicPlayerManager.isPlaying.observe(this, Observer { isPlaying ->
            Log.d("FullPlayerActivity", "Playing state changed observed: $isPlaying")
            updatePlayPauseButton(isPlaying)
        })
        
        musicPlayerManager.currentPosition.observe(this, Observer { position ->
            // Update is handled by the handler runnable for smoother updates
        })
    }private fun updateUI() {
        val currentSong = musicPlayerManager.getCurrentSong()
        Log.d("FullPlayerActivity", "updateUI called - current song: ${currentSong?.title}")
        
        currentSong?.let { song ->
            tvSongTitle.text = song.title
            tvArtistName.text = song.artist
            updateTotalTime(song.duration)
            updatePlayPauseButton(musicPlayerManager.isPlaying.value ?: false)
            updateSeekBar()
            updateShuffleButton()
            updateRepeatButton()

            // Load album art using AlbumArtUtils
            AlbumArtUtils.loadAlbumArt(this, song, ivAlbumArt)
        } ?: run {
            // Handle case where there is no current song
            Log.w("FullPlayerActivity", "No current song available")
            tvSongTitle.text = getString(R.string.no_song_playing)
            tvArtistName.text = ""
            tvCurrentTime.text = "0:00"
            tvTotalTime.text = "0:00"
            seekBar.progress = 0
            ivAlbumArt.setImageResource(R.drawable.ic_music_note)
            updatePlayPauseButton(false)
        }
    }
    
    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
        }
    }
      private fun updateSeekBar() {
        val currentPosition = musicPlayerManager.getCurrentPosition()
        val duration = musicPlayerManager.getDuration()
        
        if (duration > 0) {
            seekBar.max = duration
            seekBar.progress = currentPosition
            updateCurrentTime(currentPosition)
        }
    }
    
    private fun updateCurrentTime(timeInMillis: Int) {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        tvCurrentTime.text = String.format("%d:%02d", minutes, seconds)
    }
    
    private fun updateTotalTime(timeInMillis: Long) {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        tvTotalTime.text = String.format("%d:%02d", minutes, seconds)
    }
      private fun updateShuffleButton() {
        if (musicPlayerManager.isShuffleEnabled()) {
            btnShuffle.setColorFilter(getColor(R.color.lavender))
        } else {
            btnShuffle.setColorFilter(getColor(R.color.lavender_light))
        }
    }
    
    private fun updateRepeatButton() {
        if (musicPlayerManager.isRepeatEnabled()) {
            btnRepeat.setColorFilter(getColor(R.color.lavender))
        } else {
            btnRepeat.setColorFilter(getColor(R.color.lavender_light))
        }
    }      override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
      override fun onResume() {
        super.onResume()
        Log.d("FullPlayerActivity", "onResume - refreshing state")
        Log.d("FullPlayerActivity", "Service status: ${musicPlayerManager.getServiceStatus()}")
        
        // Refresh the UI to make sure we have the latest state
        updateUI()
        
        // Ensure we have the current song and playlist
        val currentSong = musicPlayerManager.getCurrentSong()
        Log.d("FullPlayerActivity", "Current song on resume: ${currentSong?.title}")
        
        handler.post(updateRunnable)
    }
    
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FullPlayerActivity::class.java)
            context.startActivity(intent)
        }
    }
}
