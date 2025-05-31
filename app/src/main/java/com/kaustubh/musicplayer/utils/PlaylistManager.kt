package com.kaustubh.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaustubh.musicplayer.models.Playlist
import com.kaustubh.musicplayer.models.Song

class PlaylistManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: PlaylistManager? = null
        private const val PREFS_NAME = "playlists_prefs"
        private const val KEY_PLAYLISTS = "saved_playlists"
        
        fun getInstance(context: Context): PlaylistManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlaylistManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _playlistsLiveData = MutableLiveData<List<Playlist>>()
    
    val playlistsLiveData: LiveData<List<Playlist>> = _playlistsLiveData
    
    init {
        loadPlaylists()
    }
    
    private fun loadPlaylists() {
        try {
            val playlistsJson = prefs.getString(KEY_PLAYLISTS, null)
            if (playlistsJson != null) {
                val type = object : TypeToken<List<Playlist>>() {}.type
                val playlists: List<Playlist> = gson.fromJson(playlistsJson, type)
                _playlistsLiveData.value = playlists
            } else {
                _playlistsLiveData.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _playlistsLiveData.value = emptyList()
        }
    }
      private fun savePlaylists() {
        try {
            val playlistsJson = gson.toJson(_playlistsLiveData.value ?: emptyList<Playlist>())
            prefs.edit().putString(KEY_PLAYLISTS, playlistsJson).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun createPlaylist(name: String, description: String = ""): Playlist {
        val newPlaylist = Playlist(
            name = name,
            description = description
        )
        
        val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
        currentPlaylists.add(newPlaylist)
        _playlistsLiveData.value = currentPlaylists
        savePlaylists()
        
        return newPlaylist
    }
    
    fun deletePlaylist(playlist: Playlist) {
        val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
        currentPlaylists.removeAll { it.id == playlist.id }
        _playlistsLiveData.value = currentPlaylists
        savePlaylists()
    }
      fun deletePlaylist(playlistId: Long): Boolean {
        return try {
            val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
            val removed = currentPlaylists.removeAll { it.id == playlistId }
            if (removed) {
                _playlistsLiveData.value = currentPlaylists
                savePlaylists()
            }
            removed
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun updatePlaylist(updatedPlaylist: Playlist): Boolean {
        return try {
            val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
            val index = currentPlaylists.indexOfFirst { it.id == updatedPlaylist.id }
            if (index != -1) {
                currentPlaylists[index] = updatedPlaylist
                _playlistsLiveData.value = currentPlaylists
                savePlaylists()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun addSongToPlaylist(song: Song, playlist: Playlist) {
        val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
        val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlist.id }
        
        if (playlistIndex != -1) {
            currentPlaylists[playlistIndex].addSong(song)
            _playlistsLiveData.value = currentPlaylists
            savePlaylists()
        }
    }
    
    fun removeSongFromPlaylist(song: Song, playlist: Playlist) {
        val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
        val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlist.id }
        
        if (playlistIndex != -1) {
            currentPlaylists[playlistIndex].removeSong(song)
            _playlistsLiveData.value = currentPlaylists
            savePlaylists()
        }
    }
      fun removeSongFromPlaylist(playlistId: Long, song: Song): Boolean {
        return try {
            val currentPlaylists = _playlistsLiveData.value?.toMutableList() ?: mutableListOf()
            val playlistIndex = currentPlaylists.indexOfFirst { it.id == playlistId }
            
            if (playlistIndex != -1) {
                currentPlaylists[playlistIndex].removeSong(song)
                _playlistsLiveData.value = currentPlaylists
                savePlaylists()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
      fun getPlaylistById(id: Long): Playlist? {
        return _playlistsLiveData.value?.find { it.id == id }
    }
      fun getAllPlaylists(): List<Playlist> {
        return _playlistsLiveData.value ?: emptyList()
    }
    
    fun getPlaylistsContainingSong(song: Song): List<Playlist> {
        return getAllPlaylists().filter { it.containsSong(song) }
    }
}
