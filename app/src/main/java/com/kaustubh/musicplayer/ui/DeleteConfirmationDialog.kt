package com.kaustubh.musicplayer.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song

class DeleteConfirmationDialog : DialogFragment() {
    
    companion object {
        private const val ARG_SONG_TITLE = "song_title"
        private const val ARG_SONG_ARTIST = "song_artist"
        
        fun newInstance(song: Song): DeleteConfirmationDialog {
            val dialog = DeleteConfirmationDialog()
            val args = Bundle()
            args.putString(ARG_SONG_TITLE, song.title)
            args.putString(ARG_SONG_ARTIST, song.artist)
            dialog.arguments = args
            return dialog
        }
    }
    
    interface OnDeleteConfirmedListener {
        fun onDeleteConfirmed()
        fun onDeleteCancelled()
    }
    
    private var listener: OnDeleteConfirmedListener? = null
    
    fun setOnDeleteConfirmedListener(listener: OnDeleteConfirmedListener) {
        this.listener = listener
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songTitle = arguments?.getString(ARG_SONG_TITLE) ?: "Unknown Song"
        val songArtist = arguments?.getString(ARG_SONG_ARTIST) ?: "Unknown Artist"
        
        return AlertDialog.Builder(requireContext())
            .setTitle("Delete Song")
            .setMessage("Are you sure you want to delete \"$songTitle\" by $songArtist?\n\nThis action cannot be undone.")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Delete") { _, _ ->
                listener?.onDeleteConfirmed()
                dismiss()
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener?.onDeleteCancelled()
                dismiss()
            }
            .setCancelable(true)
            .create()
    }
    
    override fun onCancel(dialog: android.content.DialogInterface) {
        super.onCancel(dialog)
        listener?.onDeleteCancelled()
    }
}
