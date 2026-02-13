package org.ghost.skycast

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import timber.log.Timber

class SkyCastApp : Application(), SingletonImageLoader.Factory{
    override fun onCreate() {
        super.onCreate()
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    /**
     * Configures the global Coil ImageLoader.
     * This setup ensures images are cached effectively for offline usage
     * and provides debug logs when developing.
     */
    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Use 25% of available memory for caching bitmaps
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of disk space for caching images
                    .build()
            }
            .apply {
                // Enable Coil logging only in Debug builds to track image loading success/failure
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .crossfade(true) // Enable crossfade animation globally
            .build()
    }



}