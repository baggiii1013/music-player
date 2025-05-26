package com.kaustubh.musicplayer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class MusicQuickSettingsTile : TileService() {

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            updateTile()
        }        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
            updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        // Try to start and bind to music service
        val intent = Intent(this, MusicService::class.java)
        try {
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            // If binding fails, service might not be running
            // Update tile to show unavailable state
            updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onClick() {
        super.onClick()
        
        if (musicService != null && musicService!!.getCurrentSong() != null) {
            // If music is playing or paused, toggle playback
            musicService!!.playPause()
            updateTile()
        } else {
            // If no music is playing, open the app
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        
        if (musicService != null && isBound) {
            val currentSong = musicService!!.getCurrentSong()
            val isPlaying = musicService!!.isPlaying()
            
            if (currentSong != null) {
                // Music is loaded
                tile.label = currentSong.title
                tile.subtitle = currentSong.artist
                
                if (isPlaying) {
                    tile.state = Tile.STATE_ACTIVE
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_pause)
                } else {
                    tile.state = Tile.STATE_INACTIVE
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_play_arrow)
                }
            } else {
                // No music loaded
                tile.label = getString(R.string.app_name)
                tile.subtitle = getString(R.string.quick_tile_no_music)
                tile.state = Tile.STATE_INACTIVE
                tile.icon = Icon.createWithResource(this, R.drawable.ic_music_note)
            }
        } else {
            // Service not connected
            tile.label = getString(R.string.app_name)
            tile.subtitle = getString(R.string.quick_tile_no_music)
            tile.state = Tile.STATE_UNAVAILABLE
            tile.icon = Icon.createWithResource(this, R.drawable.ic_music_note)
        }
        
        tile.updateTile()
    }
}
