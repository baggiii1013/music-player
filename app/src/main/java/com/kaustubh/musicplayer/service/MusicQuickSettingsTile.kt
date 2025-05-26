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
import android.util.Log
import androidx.annotation.RequiresApi
import com.kaustubh.musicplayer.MainActivity
import com.kaustubh.musicplayer.R

@RequiresApi(Build.VERSION_CODES.N)
class MusicQuickSettingsTile : TileService() {

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("QuickSettingsTile", "Service connected")
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            updateTile()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("QuickSettingsTile", "Service disconnected")
            musicService = null
            isBound = false
            updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d("QuickSettingsTile", "Tile start listening")
        // Try to start and bind to music service
        val intent = Intent(this, MusicService::class.java)
        try {
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e("QuickSettingsTile", "Failed to bind to service", e)
            // If binding fails, service might not be running
            // Update tile to show unavailable state
            updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d("QuickSettingsTile", "Tile stop listening")
        if (isBound) {
            // Unbind from service
            try {
                unbindService(serviceConnection)
                isBound = false
                musicService = null
            } catch (e: Exception) {
                Log.e("QuickSettingsTile", "Failed to unbind from service", e)
            }
        }
    }

    override fun onClick() {
        super.onClick()
        Log.d("QuickSettingsTile", "Tile clicked")

        if (isBound && musicService != null) {
            // Service is available, toggle play/pause
            Log.d("QuickSettingsTile", "Toggling playback via service")
            musicService!!.playPause()
            updateTile()
        } else {
            // Service not available, open the app
            Log.d("QuickSettingsTile", "Service not available, opening app")
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        if (isBound && musicService != null) {
            val isPlaying = musicService!!.isPlaying()
            val currentSong = musicService!!.getCurrentSong()

            Log.d("QuickSettingsTile", "Updating tile - Playing: $isPlaying, Song: ${currentSong?.title}")

            tile.state = if (isPlaying) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = currentSong?.title ?: getString(R.string.app_name)
            tile.subtitle = currentSong?.artist ?: getString(R.string.quick_tile_no_music)

            if (isPlaying) {
                tile.icon = Icon.createWithResource(this, R.drawable.ic_pause)
            } else {
                tile.icon = Icon.createWithResource(this, R.drawable.ic_play_arrow)
            }
        } else {
            Log.d("QuickSettingsTile", "Updating tile - Service not available")
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.app_name)
            tile.subtitle = getString(R.string.quick_tile_no_music)
            tile.icon = Icon.createWithResource(this, R.drawable.ic_music_note)
        }

        tile.updateTile()
    }
}
