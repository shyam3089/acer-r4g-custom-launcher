package com.shyam.acerlauncher.ui

import android.app.Activity
import android.os.Build
import android.view.View
import android.widget.ImageView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherPreferences
import com.shyam.acerlauncher.data.WallpaperCatalog

fun Activity.applyWallpaper() {
    val image = findViewById<ImageView>(R.id.wallpaper) ?: return
    val scrim = findViewById<View>(R.id.wallpaperScrim)
    val option = WallpaperCatalog.find(LauncherPreferences(this).wallpaper)
    image.setImageDrawable(null)
    image.setImageResource(option.image)
    image.visibility = View.VISIBLE
    image.scaleX = 1f
    image.scaleY = 1f
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        image.setRenderEffect(null)
    }
    scrim?.visibility = View.VISIBLE
}
