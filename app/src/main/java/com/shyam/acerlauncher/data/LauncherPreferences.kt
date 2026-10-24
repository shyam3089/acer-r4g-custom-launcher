package com.shyam.acerlauncher.data

import android.content.Context

class LauncherPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    val favorites = FavoritesRepository(SharedPreferencesStringStore(preferences))

    var wallpaper: String
        get() = preferences.getString(KEY_WALLPAPER, WALLPAPER_MOUNTAIN) ?: WALLPAPER_MOUNTAIN
        set(value) = preferences.edit().putString(KEY_WALLPAPER, value).apply()

    var lastFocusedComponent: String?
        get() = preferences.getString(KEY_LAST_FOCUS, null)
        set(value) = preferences.edit().putString(KEY_LAST_FOCUS, value).apply()

    companion object {
        private const val FILE_NAME = "launcher_preferences"
        private const val KEY_WALLPAPER = "wallpaper"
        private const val KEY_LAST_FOCUS = "last_focus"
        const val WALLPAPER_MOUNTAIN = "mountain"
        const val WALLPAPER_SOLID = "solid"
    }
}
