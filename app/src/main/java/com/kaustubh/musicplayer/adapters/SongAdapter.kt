package com.kaustubh.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.utils.AlbumArtUtils
import com.kaustubh.musicplayer.player.MusicPlayerManager

class SongAdapter(
    private var songs: MutableList<Song>,
    private val onSongClick: (Song, List<Song>) -> Unit,
    private val onShareSong: (Song) -> Unit = {},
    private val onDeleteSong: (Song) -> Unit = {}
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    
    private var currentPlayingSongId: Long? = null
    private lateinit var musicPlayerManager: MusicPlayerManager

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.song_album_art)
        val title: TextView = itemView.findViewById(R.id.song_title)
        val artist: TextView = itemView.findViewById(R.id.song_artist)
        val duration: TextView = itemView.findViewById(R.id.song_duration)
        val playingIndicator: ImageView = itemView.findViewById(R.id.playing_indicator)
        // val favoriteIndicator: ImageView = itemView.findViewById(R.id.favorite_indicator) // TODO: Implement favorite functionality
        val moreButton: ImageButton = itemView.findViewById(R.id.song_more_button)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSongClick(songs[position], songs.toList())
                }
            }
            
            moreButton.setOnClickListener { view ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(view, songs[position])
                }
            }
        }
        
        private fun showPopupMenu(anchorView: View, song: Song) {
            val popup = PopupMenu(anchorView.context, anchorView)
            popup.menuInflater.inflate(R.menu.song_options_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_share -> {
                        onShareSong(song)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteSong(song)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        
        // Initialize MusicPlayerManager if not already done
        if (!::musicPlayerManager.isInitialized) {
            musicPlayerManager = MusicPlayerManager.getInstance(parent.context)
        }
        
        return SongViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.duration.text = song.getDurationString()
        
        // Load album art using AlbumArtUtils
        AlbumArtUtils.loadAlbumArt(holder.itemView.context, song, holder.albumArt)
        
        // Show/hide playing indicator
        if (song.id == currentPlayingSongId) {
            holder.playingIndicator.visibility = View.VISIBLE
            holder.title.setTextColor(holder.itemView.context.getColor(R.color.lavender_primary))
        } else {
            holder.playingIndicator.visibility = View.GONE
            holder.title.setTextColor(holder.itemView.context.getColor(R.color.text_primary))        }
        
        // TODO: Implement favorite functionality
        // Show/hide favorite indicator
        // if (::musicPlayerManager.isInitialized && musicPlayerManager.isFavorite(song)) {
        //     holder.favoriteIndicator.visibility = View.VISIBLE
        // } else {
        //     holder.favoriteIndicator.visibility = View.GONE
        // }
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

    fun updateSongs(newSongs: List<Song>) {
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }
    
    fun removeSong(song: Song) {
        val position = songs.indexOfFirst { it.id == song.id }
        if (position != -1) {
            songs.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    
    fun refreshFavorites() {
        notifyDataSetChanged()
    }
}
