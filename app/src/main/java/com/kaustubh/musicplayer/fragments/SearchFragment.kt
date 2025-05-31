package com.kaustubh.musicplayer.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.adapters.SongAdapter
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.player.MusicPlayerManager
import com.kaustubh.musicplayer.utils.SongUtils
import com.kaustubh.musicplayer.utils.ModernSongDeleter
import com.kaustubh.musicplayer.ui.PlaylistSelectionDialog
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

class SearchFragment : Fragment() {
    
    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var musicPlayerManager: MusicPlayerManager
    private lateinit var modernSongDeleter: ModernSongDeleter
      private val deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest> = 
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()) { result ->
            modernSongDeleter.handleDeleteResult(result.resultCode)
        }
    
    private var allSongs = listOf<Song>()
    private var filteredSongs = listOf<Song>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ModernSongDeleter with the pre-registered launcher
        modernSongDeleter = ModernSongDeleter(
            requireContext(),
            parentFragmentManager,
            deleteRequestLauncher
        )
        
        initializeViews(view)
        setupSearchView()
        setupRecyclerView()
        loadSongs()
    }
      private fun initializeViews(view: View) {
        searchEditText = view.findViewById(R.id.search_edit_text)
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler)
        musicPlayerManager = MusicPlayerManager.getInstance(requireContext())
    }

    private fun setupSearchView() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSongs(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })    }    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = filteredSongs.toMutableList(),
            onSongClick = { song, playlist ->
                // Play selected song
                musicPlayerManager.playSong(song, allSongs)
            },
            onShareSong = { song ->
                SongUtils.shareSong(requireContext(), song)
            },            onDeleteSong = { song ->
                modernSongDeleter.deleteSong(song) { deletedSong ->
                    // Remove from adapter and update the lists
                    songAdapter.removeSong(deletedSong)
                    
                    // Update the main allSongs list and filtered list
                    allSongs = allSongs.filter { it.id != deletedSong.id }
                    filteredSongs = filteredSongs.filter { it.id != deletedSong.id }
                    
                    // Update MusicPlayerManager with updated song list
                    musicPlayerManager.updateAllSongs(allSongs)
                }
            },
            onAddToPlaylist = { song ->
                val dialog = PlaylistSelectionDialog.newInstance(song)
                dialog.show(parentFragmentManager, "PlaylistSelectionDialog")
            }
        )
        
        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }
      private fun loadSongs() {
        // Load all songs for searching
        musicPlayerManager.allSongsLiveData.observe(viewLifecycleOwner) { songs ->
            allSongs = songs
            if (searchEditText.text.isEmpty()) {
                updateSearchResults(songs)
            }
        }
    }
    
    private fun filterSongs(query: String) {
        filteredSongs = if (query.isEmpty()) {
            allSongs
        } else {
            allSongs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
        }
        updateSearchResults(filteredSongs)
    }
    
    private fun updateSearchResults(songs: List<Song>) {
        songAdapter.updateSongs(songs)
    }
}
