package com.kaustubh.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.kaustubh.musicplayer.R
import com.kaustubh.musicplayer.models.Song
import java.io.ByteArrayInputStream

object AlbumArtUtils {
    
    /**
     * Load album art for a song using multiple fallback methods
     */
    fun loadAlbumArt(context: Context, song: Song, imageView: ImageView) {
        // Try to load album art using Glide with custom data source
        Glide.with(context)
            .load(AlbumArtDataModel(song))
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_album_art)
                    .error(R.drawable.default_album_art)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
            )
            .into(imageView)
    }
    
    /**
     * Extract album art from audio file using MediaMetadataRetriever
     */
    fun extractAlbumArt(context: Context, song: Song): Bitmap? {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            
            // Try to set data source using URI first
            try {
                retriever.setDataSource(context, song.uri)
            } catch (e: Exception) {
                // Fallback to file path
                try {
                    retriever.setDataSource(song.path)
                } catch (e2: Exception) {
                    Log.w("AlbumArtUtils", "Failed to set data source for ${song.title}: ${e2.message}")
                    return null
                }
            }
            
            // Extract embedded album art
            val artBytes = retriever.embeddedPicture
            if (artBytes != null) {
                Log.d("AlbumArtUtils", "Found embedded album art for ${song.title}")
                return BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            } else {
                Log.d("AlbumArtUtils", "No embedded album art found for ${song.title}")
            }
            
        } catch (e: Exception) {
            Log.e("AlbumArtUtils", "Error extracting album art for ${song.title}: ${e.message}")
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                Log.e("AlbumArtUtils", "Error releasing MediaMetadataRetriever: ${e.message}")
            }
        }
        
        return null
    }
    
    /**
     * Get album art as ByteArray for Glide loading
     */
    fun getAlbumArtBytes(context: Context, song: Song): ByteArray? {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            
            // Try URI first, then file path
            try {
                retriever.setDataSource(context, song.uri)
            } catch (e: Exception) {
                try {
                    retriever.setDataSource(song.path)
                } catch (e2: Exception) {
                    return null
                }
            }
            
            return retriever.embeddedPicture
            
        } catch (e: Exception) {
            Log.e("AlbumArtUtils", "Error getting album art bytes: ${e.message}")
            return null
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Data class for Glide to load album art
     */
    data class AlbumArtDataModel(val song: Song)
}
