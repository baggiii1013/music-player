package com.kaustubh.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.ui.DeleteConfirmationDialog

class ModernSongDeleter {
    
    private var pendingSong: Song? = null
    private var pendingCallback: ((Song) -> Unit)? = null
    private var deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var context: Context? = null
    private var fragmentManager: androidx.fragment.app.FragmentManager? = null
    
    constructor(activity: FragmentActivity) {
        this.context = activity
        this.fragmentManager = activity.supportFragmentManager
        this.deleteRequestLauncher = activity.activityResultRegistry.register(
            "delete_request",
            activity,
            androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            handleDeleteResult(result.resultCode)
        }
    }
      constructor(
        context: Context, 
        fragmentManager: androidx.fragment.app.FragmentManager,
        deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        this.context = context
        this.fragmentManager = fragmentManager
        this.deleteRequestLauncher = deleteRequestLauncher
    }
    
    // Method to set the result handler for the pre-registered launcher
    fun setResultHandler(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.deleteRequestLauncher = launcher
    }
      fun deleteSong(song: Song, onDeleted: (Song) -> Unit) {
        pendingSong = song
        pendingCallback = onDeleted
        
        // Show confirmation dialog first
        val dialog = DeleteConfirmationDialog.newInstance(song)
        dialog.setOnDeleteConfirmedListener(object : DeleteConfirmationDialog.OnDeleteConfirmedListener {
            override fun onDeleteConfirmed() {
                performActualDeletion(song, onDeleted)
            }
            
            override fun onDeleteCancelled() {
                // User cancelled, clear pending state
                pendingSong = null
                pendingCallback = null
            }        })
        dialog.show(fragmentManager!!, "delete_confirmation")
    }
    
    private fun performActualDeletion(song: Song, onDeleted: (Song) -> Unit) {
        try {            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ - Request permission through system dialog
                val urisToDelete = listOf(song.uri)
                val pendingIntent = MediaStore.createDeleteRequest(context!!.contentResolver, urisToDelete)
                
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                deleteRequestLauncher!!.launch(request)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 - Try direct deletion
                tryDirectDeletion(song, onDeleted)
            } else {
                // Android 9 and below - Direct file deletion                SongUtils.deleteDirectly(context!!, song, onDeleted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context!!, "Failed to delete song: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    fun handleDeleteResult(resultCode: Int) {
        val song = pendingSong
        val callback = pendingCallback
        
        if (song != null && callback != null) {            if (resultCode == Activity.RESULT_OK) {
                // User granted permission, song should be deleted
                callback(song)
                Toast.makeText(context!!, "\"${song.title}\" deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                // User denied permission or cancelled
                Toast.makeText(context!!, "Delete cancelled by user", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Clear pending state
        pendingSong = null
        pendingCallback = null
    }
      private fun tryDirectDeletion(song: Song, onDeleted: (Song) -> Unit) {
        try {
            val deletedRows = context!!.contentResolver.delete(song.uri, null, null)
            
            if (deletedRows > 0) {
                onDeleted(song)
                Toast.makeText(context!!, "\"${song.title}\" deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Remove from app database even if file deletion failed
                onDeleted(song)
                Toast.makeText(context!!, "Song removed from library", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Remove from app database but notify about limitation
            onDeleted(song)
            Toast.makeText(context!!, "Song removed from library. File may still exist on device.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context!!, "Failed to delete song: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
