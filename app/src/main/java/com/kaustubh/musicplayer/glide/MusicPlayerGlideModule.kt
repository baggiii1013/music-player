package com.kaustubh.musicplayer.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.kaustubh.musicplayer.utils.AlbumArtModelLoader
import com.kaustubh.musicplayer.utils.AlbumArtUtils
import java.io.InputStream

@GlideModule
class MusicPlayerGlideModule : AppGlideModule() {
    
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register our custom model loader for album art
        registry.prepend(
            AlbumArtUtils.AlbumArtDataModel::class.java,
            InputStream::class.java,
            AlbumArtModelLoader.Factory(context)
        )
    }
}
