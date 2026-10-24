package com.shyam.acerlauncher.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.AppDiscovery
import com.shyam.acerlauncher.data.LauncherPreferences
import java.util.concurrent.Executors

class ManageAppsActivity : TvActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var preferences: LauncherPreferences
    private lateinit var iconLoader: AppIconLoader
    private lateinit var adapter: ManageAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        applyWallpaper()
        preferences = LauncherPreferences(this)
        iconLoader = AppIconLoader(this)
        findViewById<TextView>(R.id.screenTitle).setText(R.string.manage_home_apps)
        findViewById<TextView>(R.id.screenHint).setText(R.string.manage_hint)

        adapter = ManageAppsAdapter(iconLoader) { app -> preferences.favorites.toggle(app.component) }
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@ManageAppsActivity)
            adapter = this@ManageAppsActivity.adapter
            itemAnimator = null
        }
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        applyWallpaper()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        iconLoader.close()
        super.onDestroy()
    }

    private fun loadApps() {
        executor.execute {
            val apps = AppDiscovery(packageManager, packageName).discover()
            preferences.favorites.loadOrInitialize(apps)
            val pinned = preferences.favorites.pinnedComponents()
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                adapter.submitList(apps, pinned)
                findViewById<View>(R.id.emptyText).visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
                findViewById<RecyclerView>(R.id.list).visibility = if (apps.isEmpty()) View.GONE else View.VISIBLE
                findViewById<RecyclerView>(R.id.list).post {
                    findViewById<RecyclerView>(R.id.list)
                        .findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                }
            }
        }
    }
}
