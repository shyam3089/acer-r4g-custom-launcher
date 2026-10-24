package com.shyam.acerlauncher.ui

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import com.shyam.acerlauncher.R
import java.util.concurrent.Executors

class AppIconLoader(context: Context) : AutoCloseable {
    private val packageManager = context.packageManager
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache = object : LruCache<String, Artwork>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Artwork): Int = value.bitmap.byteCount
    }

    fun load(component: ComponentName?, vararg targets: ImageView) {
        val key = component?.flattenToString()
        targets.forEach { target ->
            target.setTag(R.id.icon_request_tag, key)
            target.setImageResource(R.drawable.ic_launcher)
        }
        if (component == null || key == null) return
        cache.get(key)?.let {
            applyArtwork(key, it, targets)
            return
        }
        executor.execute {
            val artwork = runCatching {
                @Suppress("DEPRECATION")
                val activity = packageManager.getActivityInfo(component, 0)
                val hasBanner = activity.banner != 0 || activity.applicationInfo.banner != 0
                val drawable = if (hasBanner) activity.loadBanner(packageManager) else null
                val source = drawable ?: activity.loadIcon(packageManager)
                val width = if (drawable != null) BANNER_WIDTH else ICON_SIZE
                val height = if (drawable != null) BANNER_HEIGHT else ICON_SIZE
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { output ->
                    source.setBounds(0, 0, width, height)
                    source.draw(Canvas(output))
                }
                Artwork(bitmap, drawable != null)
            }.getOrNull() ?: return@execute
            cache.put(key, artwork)
            mainHandler.post {
                applyArtwork(key, artwork, targets)
            }
        }
    }

    private fun applyArtwork(key: String, artwork: Artwork, targets: Array<out ImageView>) {
        val scaleType = if (artwork.isBanner) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
        targets.forEach { target ->
            if (target.getTag(R.id.icon_request_tag) == key) {
                target.scaleType = scaleType
                target.setImageBitmap(artwork.bitmap)
            }
        }
    }

    override fun close() {
        executor.shutdownNow()
        cache.evictAll()
    }

    companion object {
        private const val ICON_SIZE = 128
        private const val BANNER_WIDTH = 320
        private const val BANNER_HEIGHT = 180
    }

    private data class Artwork(val bitmap: Bitmap, val isBanner: Boolean)
}
