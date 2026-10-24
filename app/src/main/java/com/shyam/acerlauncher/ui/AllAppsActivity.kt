package com.shyam.acerlauncher.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.AppDiscovery
import com.shyam.acerlauncher.data.LauncherApp
import java.util.concurrent.Executors

class AllAppsActivity : TvActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var iconLoader: AppIconLoader
    private lateinit var adapter: AppGridAdapter
    private lateinit var grid: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)
        applyWallpaper()

        iconLoader = AppIconLoader(this)
        adapter = AppGridAdapter(
            iconLoader = iconLoader,
            onLaunch = ::launchApp,
            onLongPress = {},
            onBottomOverscroll = {},
        )
        grid = findViewById(R.id.appsGrid)
        grid.layoutManager = GridLayoutManager(this, COLUMN_COUNT)
        grid.adapter = adapter
        grid.itemAnimator = null
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
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                adapter.submitList(apps)
                val empty = apps.isEmpty()
                grid.visibility = if (empty) View.GONE else View.VISIBLE
                findViewById<View>(R.id.emptyText).visibility = if (empty) View.VISIBLE else View.GONE
                if (!empty) {
                    grid.post {
                        grid.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                    }
                }
            }
        }
    }

    private fun launchApp(app: LauncherApp) {
        val component = app.componentName ?: return
        val exactIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            .setComponent(component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val fallback = packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
            ?: packageManager.getLaunchIntentForPackage(app.packageName)
        runCatching { startActivity(exactIntent) }
            .recoverCatching { startActivity(fallback ?: throw it) }
            .onFailure {
                Toast.makeText(
                    this,
                    getString(R.string.unable_to_open_app, app.label),
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private companion object {
        const val COLUMN_COUNT = 4
    }
}
