package com.kaustubh.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    
    private var currentPlayingSongId: Long? = null
    
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.song_album_art)
        val title: TextView = itemView.findViewById(R.id.song_title)
        val artist: TextView = itemView.findViewById(R.id.song_artist)
        val duration: TextView = itemView.findViewById(R.id.song_duration)
        val playingIndicator: ImageView = itemView.findViewById(R.id.playing_indicator)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSongClick(songs[position])
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.duration.text = song.getDurationString()
        
        // Set album art placeholder
        holder.albumArt.setImageResource(R.drawable.default_album_art)
        
        // Show/hide playing indicator
        if (song.id == currentPlayingSongId) {
            holder.playingIndicator.visibility = View.VISIBLE
            holder.title.setTextColor(holder.itemView.context.getColor(R.color.lavender_primary))
        } else {
            holder.playingIndicator.visibility = View.GONE
            holder.title.setTextColor(holder.itemView.context.getColor(R.color.text_primary))
        }
    }
    
    override fun getItemCount(): Int = songs.size
    
    fun setCurrentPlayingSong(songId: Long?) {
        val oldPosition = songs.indexOfFirst { it.id == currentPlayingSongId }
        val newPosition = songs.indexOfFirst { it.id == songId }
        
        currentPlayingSongId = songId
        
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (newPosition != -1) {
            notifyItemChanged(newPosition)
        }
    }
}
