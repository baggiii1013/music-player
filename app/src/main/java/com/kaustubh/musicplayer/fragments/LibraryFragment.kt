package com.kaustubh.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R

class LibraryFragment : Fragment() {

    private lateinit var libraryRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLibraryView(view)
    }

    private fun setupLibraryView(view: View) {
        libraryRecyclerView = view.findViewById(R.id.library_recycler_view)
        
        // Setup quick access cards
        view.findViewById<View>(R.id.recently_played_card)?.setOnClickListener {
            // TODO: Navigate to recently played songs
        }
        
        view.findViewById<View>(R.id.liked_songs_card)?.setOnClickListener {
            // TODO: Navigate to liked songs
        }
        
        view.findViewById<View>(R.id.create_playlist_button)?.setOnClickListener {
            // TODO: Show create playlist dialog
        }
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        libraryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            // TODO: Setup library adapter for playlists, albums, etc.
        }
    }
}
