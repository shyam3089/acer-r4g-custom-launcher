package com.shyam.acerlauncher.data

import android.content.Context

class LauncherPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    val favorites = FavoritesRepository(SharedPreferencesStringStore(preferences))

    var wallpaper: String
        get() = preferences.getString(KEY_WALLPAPER, WallpaperCatalog.all.first().id)
            ?: WallpaperCatalog.all.first().id
        set(value) = preferences.edit().putString(KEY_WALLPAPER, value).apply()

    var lastFocusedComponent: String?
        get() = preferences.getString(KEY_LAST_FOCUS, null)
        set(value) = preferences.edit().putString(KEY_LAST_FOCUS, value).apply()

    companion object {
        private const val FILE_NAME = "launcher_preferences"
        private const val KEY_WALLPAPER = "wallpaper"
        private const val KEY_LAST_FOCUS = "last_focus"
    }
}
