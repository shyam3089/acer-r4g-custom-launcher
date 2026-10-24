package com.shyam.acerlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.data.AppDiscovery
import com.shyam.acerlauncher.data.LauncherApp
import com.shyam.acerlauncher.data.LauncherPreferences
import com.shyam.acerlauncher.ui.AppGridAdapter
import com.shyam.acerlauncher.ui.AppIconLoader
import com.shyam.acerlauncher.ui.FocusAnimator
import com.shyam.acerlauncher.ui.ReorderAppsActivity
import com.shyam.acerlauncher.ui.SettingsActivity
import com.shyam.acerlauncher.ui.TvActivity
import com.shyam.acerlauncher.ui.applyWallpaper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : TvActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var preferences: LauncherPreferences
    private lateinit var iconLoader: AppIconLoader
    private lateinit var adapter: AppGridAdapter
    private lateinit var grid: RecyclerView
    private lateinit var emptyState: LinearLayout
    private var receiverRegistered = false

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = updateClock()
    }
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = loadApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = LauncherPreferences(this)
        iconLoader = AppIconLoader(this)
        grid = findViewById(R.id.appsGrid)
        emptyState = findViewById(R.id.emptyState)
        adapter = AppGridAdapter(iconLoader, ::launchApp) {
            startActivity(Intent(this, ReorderAppsActivity::class.java))
        }
        grid.layoutManager = GridLayoutManager(this, 4)
        grid.adapter = adapter
        grid.itemAnimator = null

        findViewById<View>(R.id.systemSettingsButton).apply {
            FocusAnimator.attach(this)
            setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        }
        findViewById<TextView>(R.id.editButton).apply {
            FocusAnimator.attach(this)
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        }
        updateClock()
    }

    override fun onStart() {
        super.onStart()
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                this,
                timeReceiver,
                IntentFilter(Intent.ACTION_TIME_TICK),
                ContextCompat.RECEIVER_EXPORTED,
            )
            val packageFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
            ContextCompat.registerReceiver(
                this,
                packageReceiver,
                packageFilter,
                ContextCompat.RECEIVER_EXPORTED,
            )
            receiverRegistered = true
        }
    }

    override fun onResume() {
        super.onResume()
        applyWallpaper()
        updateClock()
        loadApps()
    }

    override fun onPause() {
        grid.focusedChild?.let { child ->
            val position = grid.getChildAdapterPosition(child)
            adapter.appAt(position)?.let { preferences.lastFocusedComponent = it.component }
        }
        super.onPause()
    }

    override fun onStop() {
        if (receiverRegistered) {
            unregisterReceiver(timeReceiver)
            unregisterReceiver(packageReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        iconLoader.close()
        super.onDestroy()
    }

    private fun loadApps() {
        executor.execute {
            val available = AppDiscovery(packageManager, packageName).discover()
            val pinned = preferences.favorites.loadOrInitialize(available)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                adapter.submitList(pinned)
                val empty = pinned.isEmpty()
                grid.visibility = if (empty) android.view.View.GONE else android.view.View.VISIBLE
                emptyState.visibility = if (empty) android.view.View.VISIBLE else android.view.View.GONE
                restoreFocus(pinned)
            }
        }
    }

    private fun restoreFocus(apps: List<LauncherApp>) {
        if (apps.isEmpty() || grid.hasFocus()) return
        val saved = preferences.lastFocusedComponent
        val position = apps.indexOfFirst { it.component == saved }.takeIf { it >= 0 } ?: 0
        grid.scrollToPosition(position)
        grid.post {
            grid.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
                ?: grid.requestFocus()
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
            .onFailure { Toast.makeText(this, "Unable to open ${app.label}", Toast.LENGTH_SHORT).show() }
    }

    private fun updateClock() {
        val now = Date()
        findViewById<TextView>(R.id.clockText).text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.dateText).text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
    }
}
