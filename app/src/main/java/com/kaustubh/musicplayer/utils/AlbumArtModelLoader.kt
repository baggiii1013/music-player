package com.kaustubh.musicplayer.utils

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Custom Glide ModelLoader for loading album art from audio files
 */
class AlbumArtModelLoader(private val context: Context) : ModelLoader<AlbumArtUtils.AlbumArtDataModel, InputStream> {
    
    override fun buildLoadData(
        model: AlbumArtUtils.AlbumArtDataModel,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(
            ObjectKey(model.song.id),
            AlbumArtDataFetcher(context, model.song)
        )
    }
    
    override fun handles(model: AlbumArtUtils.AlbumArtDataModel): Boolean = true
    
    /**
     * Factory for creating AlbumArtModelLoader instances
     */
    class Factory(private val context: Context) : ModelLoaderFactory<AlbumArtUtils.AlbumArtDataModel, InputStream> {
        
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AlbumArtUtils.AlbumArtDataModel, InputStream> {
            return AlbumArtModelLoader(context)
        }
        
        override fun teardown() {
            // No cleanup needed
        }
    }
}
