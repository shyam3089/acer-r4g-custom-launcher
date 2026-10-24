package com.shyam.acerlauncher.ui

import android.app.Activity
import android.view.View
import android.widget.ImageView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherPreferences

fun Activity.applyWallpaper() {
    val image = findViewById<ImageView>(R.id.wallpaper) ?: return
    val scrim = findViewById<View>(R.id.wallpaperScrim)
    val mountain = LauncherPreferences(this).wallpaper == LauncherPreferences.WALLPAPER_MOUNTAIN
    image.visibility = if (mountain) View.VISIBLE else View.GONE
    scrim?.visibility = if (mountain) View.VISIBLE else View.GONE
}
