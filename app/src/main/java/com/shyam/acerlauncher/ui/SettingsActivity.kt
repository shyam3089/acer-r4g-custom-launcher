package com.shyam.acerlauncher.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.shyam.acerlauncher.R

class SettingsActivity : TvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        applyWallpaper()

        bind(R.id.manageButton) { startActivity(Intent(this, ManageAppsActivity::class.java)) }
        bind(R.id.reorderButton) { startActivity(Intent(this, ReorderAppsActivity::class.java)) }
        bind(R.id.appearanceButton) { startActivity(Intent(this, AppearanceActivity::class.java)) }
        bind(R.id.aboutButton) {
            val about = findViewById<TextView>(R.id.aboutText)
            about.visibility = if (about.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        findViewById<View>(R.id.manageButton).requestFocus()
    }

    override fun onResume() {
        super.onResume()
        applyWallpaper()
    }

    private fun bind(id: Int, action: () -> Unit) {
        findViewById<View>(id).apply {
            FocusAnimator.attach(this)
            setOnClickListener { action() }
        }
    }
}
