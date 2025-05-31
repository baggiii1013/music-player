package com.kaustubh.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.activities.PlaylistDetailActivity
import com.kaustubh.musicplayer.adapters.PlaylistAdapter
import com.kaustubh.musicplayer.models.Playlist
import com.kaustubh.musicplayer.ui.CreatePlaylistDialog
import com.kaustubh.musicplayer.utils.PlaylistManager

class LibraryFragment : Fragment() {

    private lateinit var libraryRecyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var playlistManager: PlaylistManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize PlaylistManager
        playlistManager = PlaylistManager.getInstance(requireContext())
        
        setupLibraryView(view)
        observePlaylists()
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
            showCreatePlaylistDialog()
        }
        
        // Handle empty state create playlist button
        view.findViewById<View>(R.id.create_first_playlist_button)?.setOnClickListener {
            showCreatePlaylistDialog()
        }
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {        playlistAdapter = PlaylistAdapter(
            playlists = emptyList(),
            onPlaylistClick = { playlist ->
                // Navigate to playlist detail view
                val intent = PlaylistDetailActivity.newIntent(requireContext(), playlist.id)
                startActivity(intent)
            },
            onPlaylistMenuClick = { playlist, view ->
                showPlaylistMenu(playlist, view)
            }
        )
        
        libraryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }
    }    private fun observePlaylists() {
        playlistManager.playlistsLiveData.observe(viewLifecycleOwner, Observer { playlists ->
            playlistAdapter.updatePlaylists(playlists)
            
            // Show/hide empty state based on playlist count
            val emptyStateLayout = view?.findViewById<View>(R.id.empty_state_layout)
            if (playlists.isEmpty()) {
                emptyStateLayout?.visibility = View.VISIBLE
                libraryRecyclerView.visibility = View.GONE
            } else {
                emptyStateLayout?.visibility = View.GONE
                libraryRecyclerView.visibility = View.VISIBLE
            }
        })
    }    private fun showCreatePlaylistDialog() {
        val dialog = CreatePlaylistDialog.newInstance()
        dialog.show(parentFragmentManager, "CreatePlaylistDialog")
    }

    private fun showPlaylistMenu(playlist: Playlist, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.playlist_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete_playlist -> {
                    playlistManager.deletePlaylist(playlist.id)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
}
