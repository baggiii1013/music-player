package com.kaustubh.musicplayer.fragments

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.adapters.SongAdapter
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.player.MusicPlayerManager
import com.kaustubh.musicplayer.MainActivity
import com.kaustubh.musicplayer.utils.SongUtils
import com.kaustubh.musicplayer.utils.ModernSongDeleter
import com.kaustubh.musicplayer.ui.PlaylistSelectionDialog
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

class HomeFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var searchButton: ImageButton
    private lateinit var modernSongDeleter: ModernSongDeleter
    private val songs = mutableListOf<Song>()
      private val deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest> = 
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()) { result ->
            modernSongDeleter.handleDeleteResult(result.resultCode)
        }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ModernSongDeleter with the pre-registered launcher
        modernSongDeleter = ModernSongDeleter(
            requireContext(),
            parentFragmentManager,
            deleteRequestLauncher
        )
        
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadSongs()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.songs_recycler_view)
        searchButton = view.findViewById(R.id.search_button)
    }
    
    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            // Navigate to search fragment
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.switchToSearchTab()
            }
        }
    }    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = songs,
            onSongClick = { song, playlist ->
                // Play selected song with the full playlist
                MusicPlayerManager.getInstance(requireContext()).playSong(song, songs)
                
                // Show mini player
                (activity as? MainActivity)?.showMiniPlayer()
            },
            onShareSong = { song ->
                SongUtils.shareSong(requireContext(), song)
            },
            onDeleteSong = { song ->
                modernSongDeleter.deleteSong(song) { deletedSong ->
                    // Remove from adapter and update the main songs list
                    songAdapter.removeSong(deletedSong)
                    songs.removeAll { it.id == deletedSong.id }
                    
                    // Update MusicPlayerManager with updated song list
                    MusicPlayerManager.getInstance(requireContext()).updateAllSongs(songs)
                }
            },
            onAddToPlaylist = { song ->
                val dialog = PlaylistSelectionDialog.newInstance(song)
                dialog.show(parentFragmentManager, "PlaylistSelectionDialog")
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }private fun loadSongs() {
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
            
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            
            val cursor: Cursor? = requireActivity().contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
        
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                
                songs.clear()
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val duration = it.getLong(durationColumn)
                    val path = it.getString(pathColumn)
                    
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    val song = Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        uri = contentUri
                    )
                    songs.add(song)
                }
                
                songAdapter.notifyDataSetChanged()
                // Update MusicPlayerManager with all songs for search functionality
                MusicPlayerManager.getInstance(requireContext()).updateAllSongs(songs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error gracefully - could show a message to user
        }
    }
}
