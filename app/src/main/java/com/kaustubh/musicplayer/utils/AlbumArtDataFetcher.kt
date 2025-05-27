package com.kaustubh.musicplayer.utils

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.kaustubh.musicplayer.models.Song
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * DataFetcher for loading album art from audio files
 */
class AlbumArtDataFetcher(
    private val context: Context,
    private val song: Song
) : DataFetcher<InputStream> {
    
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            val albumArtBytes = AlbumArtUtils.getAlbumArtBytes(context, song)
            if (albumArtBytes != null) {
                val inputStream = ByteArrayInputStream(albumArtBytes)
                callback.onDataReady(inputStream)
            } else {
                callback.onLoadFailed(Exception("No album art found"))
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }
    
    override fun cleanup() {
        // No cleanup needed for ByteArrayInputStream
    }
    
    override fun cancel() {
        // No cancellation needed
    }
    
    override fun getDataClass(): Class<InputStream> = InputStream::class.java
    
    override fun getDataSource(): DataSource = DataSource.LOCAL
}
