package com.shyam.acerlauncher.ui

import android.os.Bundle
import android.view.View
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.LauncherPreferences

class AppearanceActivity : TvActivity() {
    private lateinit var preferences: LauncherPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance)
        preferences = LauncherPreferences(this)
        applyWallpaper()

        bind(R.id.mountainButton, LauncherPreferences.WALLPAPER_MOUNTAIN)
        bind(R.id.solidButton, LauncherPreferences.WALLPAPER_SOLID)
        findViewById<View>(
            if (preferences.wallpaper == LauncherPreferences.WALLPAPER_SOLID) R.id.solidButton else R.id.mountainButton
        ).requestFocus()
    }

    private fun bind(id: Int, value: String) {
        findViewById<View>(id).apply {
            FocusAnimator.attach(this)
            setOnClickListener {
                preferences.wallpaper = value
                applyWallpaper()
            }
        }
    }
}
