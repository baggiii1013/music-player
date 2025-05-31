package com.kaustubh.musicplayer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Playlist
import com.kaustubh.musicplayer.utils.PlaylistManager

class CreatePlaylistDialog : DialogFragment() {
    
    private lateinit var etPlaylistName: EditText
    private lateinit var etPlaylistDescription: EditText
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button
    
    private lateinit var playlistManager: PlaylistManager
    private var onPlaylistCreatedListener: OnPlaylistCreatedListener? = null
    
    interface OnPlaylistCreatedListener {
        fun onPlaylistCreated(playlist: Playlist)
    }
    
    fun setOnPlaylistCreatedListener(listener: OnPlaylistCreatedListener) {
        this.onPlaylistCreatedListener = listener
    }    companion object {
        fun newInstance(): CreatePlaylistDialog {
            return CreatePlaylistDialog()
        }
        
        fun newInstance(callback: (Playlist) -> Unit): CreatePlaylistDialog {
            return CreatePlaylistDialog().apply {
                setOnPlaylistCreatedListener(object : OnPlaylistCreatedListener {
                    override fun onPlaylistCreated(playlist: Playlist) {
                        callback(playlist)
                    }
                })
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_create_playlist, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        playlistManager = PlaylistManager.getInstance(requireContext())
        
        initViews(view)
        setupClickListeners()
    }
    
    private fun initViews(view: View) {
        etPlaylistName = view.findViewById(R.id.et_playlist_name)
        etPlaylistDescription = view.findViewById(R.id.et_playlist_description)
        btnCreate = view.findViewById(R.id.btn_create_playlist)
        btnCancel = view.findViewById(R.id.btn_cancel)
    }
    
    private fun setupClickListeners() {
        btnCreate.setOnClickListener {
            createPlaylist()
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
    }    private fun createPlaylist() {
        val name = etPlaylistName.text.toString().trim()
        val description = etPlaylistDescription.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter a playlist name", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if playlist name already exists
        val existingPlaylists = playlistManager.getAllPlaylists()
        if (existingPlaylists.any { it.name.equals(name, ignoreCase = true) }) {
            Toast.makeText(context, "A playlist with this name already exists", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create the playlist
        val newPlaylist = playlistManager.createPlaylist(name, description)
        onPlaylistCreatedListener?.onPlaylistCreated(newPlaylist)
        Toast.makeText(context, "Playlist '$name' created successfully", Toast.LENGTH_SHORT).show()
        dismiss()
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
