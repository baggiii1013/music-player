package com.kaustubh.musicplayer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.adapters.SongAdapter
import com.kaustubh.musicplayer.databinding.ActivityPlaylistDetailBinding
import com.kaustubh.musicplayer.models.Playlist
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.utils.PlaylistManager
import com.kaustubh.musicplayer.player.MusicPlayerManager
import com.kaustubh.musicplayer.MainActivity
import com.kaustubh.musicplayer.ui.MiniPlayerController

class PlaylistDetailActivity : AppCompatActivity(), SongAdapter.SongClickListener {
    private lateinit var binding: ActivityPlaylistDetailBinding
    private lateinit var songAdapter: SongAdapter
    private lateinit var playlist: Playlist
    private lateinit var musicPlayerManager: MusicPlayerManager
    private lateinit var miniPlayerController: MiniPlayerController
    private var playlistId: Long = 0L
    
    companion object {
        private const val EXTRA_PLAYLIST_ID = "playlist_id"
        
        fun newIntent(context: Context, playlistId: Long): Intent {
            return Intent(context, PlaylistDetailActivity::class.java).apply {
                putExtra(EXTRA_PLAYLIST_ID, playlistId)
            }
        }
    }
      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
          // Initialize MusicPlayerManager
        musicPlayerManager = MusicPlayerManager.getInstance(this)
        
        // Initialize MiniPlayerController
        val miniPlayerView = findViewById<android.view.View>(R.id.mini_player)
        miniPlayerController = MiniPlayerController(miniPlayerView, this, this)
          playlistId = intent.getLongExtra(EXTRA_PLAYLIST_ID, 0L)
        if (playlistId == 0L) {
            finish()
            return
        }
          setupToolbar()
        setupRecyclerView()
        observePlaylist()
        observeMusicPlayer()
        
        // Load initial playlist data
        loadPlaylist()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(mutableListOf(), this)
        binding.recyclerViewSongs.apply {
            layoutManager = LinearLayoutManager(this@PlaylistDetailActivity)
            adapter = songAdapter
        }
    }    private fun observePlaylist() {
        PlaylistManager.getInstance(this).playlistsLiveData.observe(this, Observer { playlists ->
            val updatedPlaylist = playlists.find { it.id == playlistId }
            if (updatedPlaylist != null) {
                updatePlaylistData(updatedPlaylist)
            } else {
                // Playlist was deleted
                finish()
            }
        })
    }
    
    private fun observeMusicPlayer() {
        // Observe current song to show/hide mini player automatically
        musicPlayerManager.currentSongLiveData.observe(this, Observer { song ->
            if (song != null) {
                miniPlayerController.show()
            } else {
                miniPlayerController.hide()
            }
        })
    }
    
    private fun loadPlaylist() {
        val foundPlaylist = PlaylistManager.getInstance(this).getPlaylistById(playlistId)
        if (foundPlaylist != null) {
            updatePlaylistData(foundPlaylist)
        } else {
            Toast.makeText(this, "Playlist not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun updatePlaylistData(updatedPlaylist: Playlist) {
        playlist = updatedPlaylist
        
        // Update toolbar title
        supportActionBar?.title = playlist.name
        
        // Update UI
        binding.apply {
            textPlaylistName.text = playlist.name
            textPlaylistDescription.text = if (playlist.description.isNotEmpty()) {
                playlist.description
            } else {
                "No description"
            }
            textSongCount.text = "${playlist.getSongCount()} songs"
            textDuration.text = playlist.getDurationString()
        }
        
        // Update songs list
        songAdapter.updateSongs(playlist.songs.toMutableList())
        
        // Show/hide empty state
        if (playlist.songs.isEmpty()) {
            binding.layoutEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerViewSongs.visibility = android.view.View.GONE
        } else {
            binding.layoutEmptyState.visibility = android.view.View.GONE
            binding.recyclerViewSongs.visibility = android.view.View.VISIBLE
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.playlist_detail_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit_playlist -> {
                showEditPlaylistDialog()
                true
            }
            R.id.action_delete_playlist -> {
                showDeletePlaylistDialog()
                true
            }
            R.id.action_clear_playlist -> {
                showClearPlaylistDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showEditPlaylistDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_playlist, null)
        val editTextName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextPlaylistName)
        val editTextDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextPlaylistDescription)
        
        editTextName.setText(playlist.name)
        editTextDescription.setText(playlist.description)
        
        AlertDialog.Builder(this)
            .setTitle("Edit Playlist")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = editTextName.text.toString().trim()
                val newDescription = editTextDescription.text.toString().trim()
                
                if (newName.isNotEmpty()) {
                    val updatedPlaylist = playlist.copy(
                        name = newName,
                        description = newDescription
                    )
                    
                    val success = PlaylistManager.getInstance(this).updatePlaylist(updatedPlaylist)
                    if (success) {
                        Toast.makeText(this, "Playlist updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update playlist", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeletePlaylistDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete \"${playlist.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val success = PlaylistManager.getInstance(this).deletePlaylist(playlist.id)
                if (success) {
                    Toast.makeText(this, "Playlist deleted", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to delete playlist", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showClearPlaylistDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Playlist")
            .setMessage("Are you sure you want to remove all songs from \"${playlist.name}\"?")            .setPositiveButton("Clear") { _, _ ->
                val clearedPlaylist = playlist.copy(songs = mutableListOf())
                val success = PlaylistManager.getInstance(this).updatePlaylist(clearedPlaylist)
                if (success) {
                    Toast.makeText(this, "Playlist cleared", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to clear playlist", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }    // SongAdapter.SongClickListener implementation
    override fun onSongClick(song: Song, songList: List<Song>) {
        // Play the selected song with the playlist as context
        musicPlayerManager.playSong(song, playlist.songs)
        
        // Show mini player when song starts playing
        miniPlayerController.show()
        
        Toast.makeText(this, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSongLongClick(song: Song): Boolean {
        showSongOptionsDialog(song)
        return true
    }
    
    private fun showSongOptionsDialog(song: Song) {
        val options = arrayOf("Remove from playlist", "Share")
        
        AlertDialog.Builder(this)
            .setTitle(song.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> removeSongFromPlaylist(song)
                    1 -> shareSong(song)
                }
            }
            .show()
    }
    
    private fun removeSongFromPlaylist(song: Song) {
        val success = PlaylistManager.getInstance(this).removeSongFromPlaylist(playlist.id, song)
        if (success) {
            Toast.makeText(this, "Song removed from playlist", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to remove song", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareSong(song: Song) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this song: ${song.title} by ${song.artist}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Song"))
    }
}
