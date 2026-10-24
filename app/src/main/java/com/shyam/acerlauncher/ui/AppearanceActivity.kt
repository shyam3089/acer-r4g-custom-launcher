package com.shyam.acerlauncher.ui

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherPreferences
import com.shyam.acerlauncher.data.WallpaperCatalog

class AppearanceActivity : TvActivity() {
    private lateinit var preferences: LauncherPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance)
        preferences = LauncherPreferences(this)
        applyWallpaper()
        findViewById<RecyclerView>(R.id.wallpaperGrid).apply {
            layoutManager = GridLayoutManager(this@AppearanceActivity, 3)
            adapter = WallpaperAdapter(WallpaperCatalog.all, preferences.wallpaper) { option ->
                preferences.wallpaper = option.id
                applyWallpaper()
            }
            itemAnimator = null
            post { findViewHolderForAdapterPosition(WallpaperCatalog.all.indexOf(WallpaperCatalog.find(preferences.wallpaper)))?.itemView?.requestFocus() }
        }
    }
}
