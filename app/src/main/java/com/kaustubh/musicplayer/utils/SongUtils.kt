package com.kaustubh.musicplayer.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song
import com.kaustubh.musicplayer.ui.DeleteConfirmationDialog
import java.io.File

object SongUtils {
    
    /**
     * Share a song by creating an intent with song information
     */
    fun shareSong(context: Context, song: Song) {
        try {
            val shareText = "🎵 ${song.title}\n" +
                    "🎤 ${song.artist}\n" +
                    "💿 ${song.album}\n" +
                    "⏱️ ${song.getDurationString()}\n\n" +
                    "Shared from Music Player"
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
            }
            
            val chooser = Intent.createChooser(shareIntent, "Share song via...")
            context.startActivity(chooser)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to share song", Toast.LENGTH_SHORT).show()
        }
    }    /**
     * Delete a song file with user confirmation using modern dialog fragment
     */
    fun deleteSong(context: Context, song: Song, onDeleted: (Song) -> Unit) {
        if (context is FragmentActivity) {
            val dialog = DeleteConfirmationDialog.newInstance(song)
            dialog.setOnDeleteConfirmedListener(object : DeleteConfirmationDialog.OnDeleteConfirmedListener {
                override fun onDeleteConfirmed() {
                    // Use simplified deletion method
                    performDelete(context, song, onDeleted)
                }
                
                override fun onDeleteCancelled() {
                    // User cancelled, do nothing
                }
            })
            dialog.show(context.supportFragmentManager, "delete_confirmation")
        } else {
            // Fallback for non-FragmentActivity contexts
            performDelete(context, song, onDeleted)
        }
    }
      private fun performDelete(context: Context, song: Song, onDeleted: (Song) -> Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (API 29+) - Use MediaStore deletion
                deleteWithMediaStore(context, song, onDeleted)
            } else {
                // Android 9 and below - Direct file deletion
                deleteDirectly(context, song, onDeleted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to delete song: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
      private fun deleteWithMediaStore(context: Context, song: Song, onDeleted: (Song) -> Unit) {
        try {
            // Try direct MediaStore deletion first
            val deletedRows = context.contentResolver.delete(song.uri, null, null)
            
            if (deletedRows > 0) {
                onDeleted(song)
                Toast.makeText(context, "\"${song.title}\" deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                // File might be protected or doesn't exist, remove from library anyway
                onDeleted(song)
                Toast.makeText(context, "Song removed from library", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            // On newer Android versions, we might not have permission
            // Remove from app's database but notify user about the limitation
            onDeleted(song)
            Toast.makeText(context, "Song removed from library. File may still exist on device.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to delete song: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
      fun deleteDirectly(context: Context, song: Song, onDeleted: (Song) -> Unit) {
        try {
            val file = File(song.path)
            
            if (file.exists()) {
                val deleted = file.delete()
                
                if (deleted) {
                    // Notify media scanner that file was deleted
                    try {
                        context.contentResolver.delete(song.uri, null, null)
                    } catch (e: Exception) {
                        // Continue even if media store deletion fails
                        e.printStackTrace()
                    }
                    
                    onDeleted(song)
                    Toast.makeText(context, "\"${song.title}\" deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // File doesn't exist, remove from database anyway
                try {
                    context.contentResolver.delete(song.uri, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                onDeleted(song)
                Toast.makeText(context, "Song removed from library", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Permission denied to delete file", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to delete song: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
