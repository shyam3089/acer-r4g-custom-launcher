package com.shyam.acerlauncher.data

import android.content.Intent
import android.content.pm.PackageManager
import java.util.Locale

class AppDiscovery(
    private val packageManager: PackageManager,
    private val ownPackage: String,
) {
    @Suppress("DEPRECATION")
    fun discover(): List<LauncherApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val candidates = packageManager.queryIntentActivities(intent, 0).mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            if (!activity.enabled || activity.packageName == ownPackage) return@mapNotNull null
            LauncherApp(
                component = "${activity.packageName}/${activity.name}",
                packageName = activity.packageName,
                label = info.loadLabel(packageManager).toString().ifBlank { activity.packageName },
            )
        }
        return deduplicateAndSort(candidates)
    }

    companion object {
        fun deduplicateAndSort(apps: List<LauncherApp>): List<LauncherApp> = apps
            .distinctBy(LauncherApp::component)
            .sortedWith(compareBy { it.label.lowercase(Locale.getDefault()) })
    }
}
