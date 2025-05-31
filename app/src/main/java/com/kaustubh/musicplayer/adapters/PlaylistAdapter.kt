package com.kaustubh.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Playlist

class PlaylistAdapter(
    private var playlists: List<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onPlaylistMenuClick: (Playlist, View) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        private val playlistInfo: TextView = itemView.findViewById(R.id.playlist_info)
        private val playlistMenu: ImageButton = itemView.findViewById(R.id.playlist_menu)

        fun bind(playlist: Playlist) {
            playlistName.text = playlist.name
            
            val songCount = playlist.getSongCount()
            val duration = playlist.getDurationString()
            playlistInfo.text = if (songCount > 0) {
                "$songCount songs • $duration"
            } else {
                "No songs"
            }

            itemView.setOnClickListener {
                onPlaylistClick(playlist)
            }

            playlistMenu.setOnClickListener {
                onPlaylistMenuClick(playlist, it)
            }
        }
    }
}
