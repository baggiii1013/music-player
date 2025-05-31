package com.kaustubh.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Playlist

class PlaylistSelectionAdapter(
    private val onPlaylistSelected: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionAdapter.PlaylistSelectionViewHolder>() {

    private var playlists: List<Playlist> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSelectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_selection, parent, false)
        return PlaylistSelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistSelectionViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    inner class PlaylistSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistName: TextView = itemView.findViewById(R.id.playlist_selection_name)
        private val playlistInfo: TextView = itemView.findViewById(R.id.playlist_selection_info)

        fun bind(playlist: Playlist) {
            playlistName.text = playlist.name
            
            val songCount = playlist.getSongCount()
            playlistInfo.text = if (songCount > 0) {
                "$songCount songs"
            } else {
                "Empty playlist"
            }

            itemView.setOnClickListener {
                onPlaylistSelected(playlist)
            }
        }
    }
}
