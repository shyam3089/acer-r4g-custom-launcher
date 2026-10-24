package com.shyam.acerlauncher.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.R
import com.shyam.acerlauncher.data.AppDiscovery
import com.shyam.acerlauncher.data.LauncherPreferences
import java.util.concurrent.Executors

class ReorderAppsActivity : TvActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var preferences: LauncherPreferences
    private lateinit var iconLoader: AppIconLoader
    private lateinit var adapter: ReorderAppsAdapter
    private lateinit var list: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        applyWallpaper()
        preferences = LauncherPreferences(this)
        iconLoader = AppIconLoader(this)
        findViewById<TextView>(R.id.screenTitle).setText(R.string.reorder_apps)
        findViewById<TextView>(R.id.screenHint).setText(R.string.reorder_hint)

        adapter = ReorderAppsAdapter(
            iconLoader = iconLoader,
            onToggleMoving = { position -> adapter.toggleMoving(position) },
            onKey = ::handleReorderKey,
        )
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.itemAnimator = null
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
            val available = AppDiscovery(packageManager, packageName).discover()
            val pinned = preferences.favorites.loadOrInitialize(available)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                adapter.submitList(pinned)
                val empty = pinned.isEmpty()
                list.visibility = if (empty) View.GONE else View.VISIBLE
                findViewById<View>(R.id.emptyText).visibility = if (empty) View.VISIBLE else View.GONE
                list.post { list.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus() }
            }
        }
    }

    private fun handleReorderKey(view: View, keyCode: Int, event: KeyEvent, position: Int): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || adapter.movingPosition == RecyclerView.NO_POSITION) return false
        val target = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> position - 1
            KeyEvent.KEYCODE_DPAD_DOWN -> position + 1
            else -> return false
        }
        if (target !in 0 until adapter.itemCount) return true
        adapter.move(position, target)
        preferences.favorites.saveComponents(adapter.components())
        list.scrollToPosition(target)
        view.post { view.requestFocus() }
        return true
    }
}
