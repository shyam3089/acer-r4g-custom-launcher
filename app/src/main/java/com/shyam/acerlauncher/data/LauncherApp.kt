package com.shyam.acerlauncher.data

import android.content.ComponentName

data class LauncherApp(
    val component: String,
    val packageName: String,
    val label: String,
) {
    val componentName: ComponentName?
        get() = ComponentName.unflattenFromString(component)
}
