package com.shyam.acerlauncher.data

import android.content.SharedPreferences

interface StringStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
}

class SharedPreferencesStringStore(
    private val preferences: SharedPreferences,
) : StringStore {
    override fun get(key: String): String? = preferences.getString(key, null)

    override fun put(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }
}
