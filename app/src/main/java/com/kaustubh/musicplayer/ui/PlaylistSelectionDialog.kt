package com.kaustubh.musicplayer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.adapters.PlaylistSelectionAdapter
import com.kaustubh.musicplayer.models.Playlist
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.utils.PlaylistManager

class PlaylistSelectionDialog : DialogFragment() {

    private lateinit var playlistManager: PlaylistManager
    private lateinit var song: Song
    private lateinit var playlistAdapter: PlaylistSelectionAdapter

    companion object {
        private const val ARG_SONG = "song"

        fun newInstance(song: Song): PlaylistSelectionDialog {
            val dialog = PlaylistSelectionDialog()
            val args = Bundle().apply {
                putParcelable(ARG_SONG, song)
            }
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistManager = PlaylistManager.getInstance(requireContext())
        song = arguments?.getParcelable(ARG_SONG) ?: throw IllegalArgumentException("Song is required")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_playlist_selection, null)
        
        setupRecyclerView(view)
        observePlaylists()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add to Playlist")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("New Playlist") { _, _ ->
                showCreatePlaylistDialog()
            }
            .create()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.playlist_selection_recycler)
        
        playlistAdapter = PlaylistSelectionAdapter { playlist ->
            addSongToPlaylist(playlist)
            dismiss()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }
    }    private fun observePlaylists() {
        playlistManager.playlistsLiveData.observe(this, Observer { playlists ->
            playlistAdapter.updatePlaylists(playlists)
        })
    }

    private fun addSongToPlaylist(playlist: Playlist) {
        playlistManager.addSongToPlaylist(song, playlist)
    }    private fun showCreatePlaylistDialog() {
        val createDialog = CreatePlaylistDialog.newInstance { newPlaylist ->
            playlistManager.addSongToPlaylist(song, newPlaylist)
        }
        createDialog.show(parentFragmentManager, "CreatePlaylistDialog")
    }
}
