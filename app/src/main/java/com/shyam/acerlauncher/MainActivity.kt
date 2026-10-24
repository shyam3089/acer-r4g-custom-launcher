package com.shyam.acerlauncher

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shyam.acerlauncher.data.AppDiscovery
import com.shyam.acerlauncher.data.LauncherApp
import com.shyam.acerlauncher.data.LauncherPreferences
import com.shyam.acerlauncher.ui.AllAppsActivity
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
import kotlin.math.roundToInt

class MainActivity : TvActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var preferences: LauncherPreferences
    private lateinit var iconLoader: AppIconLoader
    private lateinit var adapter: AppGridAdapter
    private lateinit var grid: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var quickSettingsButton: View
    private lateinit var quickSettingsOverlay: View
    private lateinit var quickSettingsPanel: View
    private lateinit var quickSettingsDismissArea: View
    private lateinit var quickSettingsBlur: ImageView
    private val statsHandler = Handler(Looper.getMainLooper())
    private var previousProcessCpuMs = 0L
    private var previousElapsedMs = 0L
    private var quickSettingsSnapshot: Bitmap? = null
    private var receiverRegistered = false

    private val statsRunnable = object : Runnable {
        override fun run() {
            val hasCpuSample = updateSystemStats()
            statsHandler.postDelayed(this, if (hasCpuSample) 3_000L else 1_000L)
        }
    }

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
        adapter = AppGridAdapter(
            iconLoader = iconLoader,
            onLaunch = ::launchApp,
            onLongPress = { startActivity(Intent(this, ReorderAppsActivity::class.java)) },
            onBottomOverscroll = ::animateBottomOverscroll,
        )
        grid.layoutManager = GridLayoutManager(this, 4)
        grid.adapter = adapter
        grid.itemAnimator = null

        quickSettingsButton = findViewById(R.id.quickSettingsButton)
        quickSettingsOverlay = findViewById(R.id.quickSettingsOverlay)
        quickSettingsPanel = findViewById(R.id.quickSettingsPanel)
        quickSettingsDismissArea = findViewById(R.id.quickSettingsDismissArea)
        quickSettingsBlur = findViewById(R.id.quickSettingsBlur)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            quickSettingsBlur.setRenderEffect(
                RenderEffect.createBlurEffect(28f, 28f, Shader.TileMode.CLAMP),
            )
        }
        quickSettingsButton.apply {
            FocusAnimator.attach(this)
            setOnClickListener { showQuickSettings() }
        }
        quickSettingsDismissArea.setOnClickListener {
            closeQuickSettings()
        }
        bindQuickSetting(R.id.quickNetworkButton, Intent(Settings.ACTION_WIFI_SETTINGS))
        bindQuickSetting(
            R.id.quickBluetoothButton,
            Intent("com.google.android.intent.action.CONNECT_INPUT"),
        )
        bindQuickSetting(R.id.quickDisplaySoundButton, Intent(Settings.ACTION_SOUND_SETTINGS))
        bindQuickSetting(R.id.quickAndroidSettingsButton, Intent(Settings.ACTION_SETTINGS))

        findViewById<View>(R.id.systemSettingsButton).apply {
            FocusAnimator.attach(this)
            setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        }
        findViewById<View>(R.id.editButton).apply {
            FocusAnimator.attach(this)
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        }
        findViewById<View>(R.id.allAppsButton).apply {
            FocusAnimator.attach(this)
            setOnClickListener { startActivity(Intent(this@MainActivity, AllAppsActivity::class.java)) }
        }
        updateClock()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && quickSettingsOverlay.visibility == View.VISIBLE) {
            closeQuickSettings()
            return true
        }
        return super.onKeyUp(keyCode, event)
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
        statsHandler.removeCallbacks(statsRunnable)
        statsHandler.post(statsRunnable)
    }

    override fun onPause() {
        statsHandler.removeCallbacks(statsRunnable)
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
        quickSettingsBlur.setImageDrawable(null)
        quickSettingsSnapshot?.recycle()
        quickSettingsSnapshot = null
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

    private fun bindQuickSetting(id: Int, intent: Intent) {
        findViewById<View>(id).apply {
            FocusAnimator.attach(this, focusedScale = 1.025f)
            setOnClickListener { openQuickSetting(intent) }
        }
    }

    private fun showQuickSettings() {
        captureQuickSettingsBlur()
        quickSettingsOverlay.visibility = View.VISIBLE
        quickSettingsDismissArea.alpha = 0f
        quickSettingsDismissArea.animate().alpha(1f).setDuration(260L).start()
        quickSettingsPanel.animate().cancel()
        quickSettingsPanel.post {
            quickSettingsPanel.translationX = -quickSettingsPanel.width.toFloat()
            quickSettingsPanel.alpha = 0.72f
            quickSettingsPanel.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(360L)
                .setInterpolator(PathInterpolator(0.16f, 1f, 0.3f, 1f))
                .start()
            quickSettingsPanel.postDelayed({
                if (quickSettingsOverlay.visibility == View.VISIBLE) {
                    findViewById<View>(R.id.quickNetworkButton).requestFocus()
                }
            }, 140L)
        }
    }

    private fun closeQuickSettings() {
        quickSettingsDismissArea.animate().cancel()
        quickSettingsDismissArea.animate().alpha(0f).setDuration(180L).start()
        quickSettingsPanel.animate().cancel()
        quickSettingsPanel.animate()
            .translationX(-quickSettingsPanel.width.toFloat())
            .alpha(0.82f)
            .setDuration(180L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                quickSettingsOverlay.visibility = View.GONE
                quickSettingsDismissArea.alpha = 1f
                quickSettingsPanel.translationX = 0f
                quickSettingsPanel.alpha = 1f
                quickSettingsButton.requestFocus()
            }
            .start()
    }

    private fun openQuickSetting(intent: Intent) {
        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        quickSettingsOverlay.visibility = View.GONE
        quickSettingsPanel.translationX = 0f
        quickSettingsPanel.alpha = 1f
        startActivity(intent)
    }

    private fun captureQuickSettingsBlur() {
        val root = window.decorView
        if (root.width <= 0 || root.height <= 0) return
        val density = resources.displayMetrics.density
        val margin = (22f * density).roundToInt()
        val panelWidth = (430f * density).roundToInt()
        val panelHeight = root.height - (margin * 2)
        val scale = 0.25f
        val snapshot = Bitmap.createBitmap(
            (panelWidth * scale).roundToInt().coerceAtLeast(1),
            (panelHeight * scale).roundToInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        Canvas(snapshot).apply {
            scale(scale, scale)
            translate(-margin.toFloat(), -margin.toFloat())
            root.draw(this)
        }
        quickSettingsBlur.setImageDrawable(null)
        quickSettingsSnapshot?.recycle()
        quickSettingsSnapshot = snapshot
        quickSettingsBlur.setImageBitmap(snapshot)
    }

    private fun updateSystemStats(): Boolean {
        val memory = ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memory)
        val ramPercent = if (memory.totalMem > 0L) {
            (((memory.totalMem - memory.availMem) * 100.0) / memory.totalMem)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            0
        }
        val cpuPercent = readCpuPercent()
        findViewById<TextView>(R.id.systemStatsText).text = if (cpuPercent == null) {
            getString(R.string.system_stats_loading).replace("RAM --", "RAM $ramPercent")
        } else {
            getString(R.string.system_stats, cpuPercent, ramPercent)
        }
        return cpuPercent != null
    }

    private fun readCpuPercent(): Int? {
        val processCpuMs = Process.getElapsedCpuTime()
        val elapsedMs = SystemClock.elapsedRealtime()
        val hasPreviousSample = previousElapsedMs != 0L
        val cpuDelta = processCpuMs - previousProcessCpuMs
        val elapsedDelta = elapsedMs - previousElapsedMs
        previousProcessCpuMs = processCpuMs
        previousElapsedMs = elapsedMs
        if (!hasPreviousSample || elapsedDelta <= 0L) return null
        return ((cpuDelta * 100.0) / elapsedDelta).roundToInt().coerceIn(0, 100)
    }

    private fun animateBottomOverscroll() {
        val resistedPull = -12f * resources.displayMetrics.density
        grid.animate().cancel()
        grid.animate()
            .translationY(resistedPull)
            .setDuration(90L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                grid.animate()
                    .translationY(0f)
                    .setDuration(260L)
                    .setInterpolator(OvershootInterpolator(1.6f))
                    .start()
            }
            .start()
    }

    private fun updateClock() {
        val now = Date()
        findViewById<TextView>(R.id.clockText).text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.dateText).text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
    }
}
