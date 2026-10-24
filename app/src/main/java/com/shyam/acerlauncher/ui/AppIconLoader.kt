package com.shyam.acerlauncher.ui

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import com.shyam.acerlauncher.R
import java.util.concurrent.Executors

class AppIconLoader(context: Context) : AutoCloseable {
    private val packageManager = context.packageManager
    private val resources = context.resources
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache = object : LruCache<String, Bitmap>(6 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun load(component: ComponentName?, target: ImageView) {
        val key = component?.flattenToString()
        target.setTag(R.id.icon_request_tag, key)
        target.setImageResource(R.drawable.ic_launcher)
        if (component == null || key == null) return
        cache.get(key)?.let {
            target.setImageBitmap(it)
            return
        }
        executor.execute {
            val bitmap = runCatching {
                val drawable = packageManager.getActivityIcon(component)
                if (drawable is BitmapDrawable && drawable.bitmap.width <= ICON_SIZE * 2) {
                    drawable.bitmap
                } else {
                    Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888).also { output ->
                        drawable.setBounds(0, 0, ICON_SIZE, ICON_SIZE)
                        drawable.draw(Canvas(output))
                    }
                }
            }.getOrNull() ?: return@execute
            cache.put(key, bitmap)
            mainHandler.post {
                if (target.getTag(R.id.icon_request_tag) == key) target.setImageBitmap(bitmap)
            }
        }
    }

    override fun close() {
        executor.shutdownNow()
        cache.evictAll()
    }

    companion object {
        private const val ICON_SIZE = 128
    }
}
