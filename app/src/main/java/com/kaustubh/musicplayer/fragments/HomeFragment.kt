package com.kaustubh.musicplayer.fragments

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.adapters.SongAdapter
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.player.MusicPlayerManager

class HomeFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songs = mutableListOf<Song>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        loadSongs()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.songs_recycler_view)
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songs) { song ->
            // Play selected song
            MusicPlayerManager.getInstance(requireContext()).playSong(song)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }
    
    private fun loadSongs() {
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
        }
    }
}
