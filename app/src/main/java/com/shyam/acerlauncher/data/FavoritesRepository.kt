package com.shyam.acerlauncher.data

class FavoritesRepository(
    private val store: StringStore,
) {
    fun loadOrInitialize(available: List<LauncherApp>): List<LauncherApp> {
        val byComponent = available.associateBy(LauncherApp::component)
        val initialized = store.get(KEY_INITIALIZED) == "1"
        val stored = PinnedAppsCodec.decode(store.get(KEY_COMPONENTS))
        val components = if (initialized) {
            stored.filter(byComponent::containsKey)
        } else {
            InitialFavorites.choose(available).map(LauncherApp::component)
        }
        if (!initialized || components != stored) {
            saveComponents(components)
        }
        return components.mapNotNull(byComponent::get)
    }

    fun pinnedComponents(): List<String> = PinnedAppsCodec.decode(store.get(KEY_COMPONENTS))

    fun toggle(component: String): Boolean {
        val current = pinnedComponents().toMutableList()
        val added = if (component in current) {
            current.remove(component)
            false
        } else {
            current.add(component)
            true
        }
        saveComponents(current)
        return added
    }

    fun move(from: Int, to: Int): List<String> {
        val current = pinnedComponents().toMutableList()
        if (from !in current.indices || to !in current.indices || from == to) return current
        current.add(to, current.removeAt(from))
        saveComponents(current)
        return current
    }

    fun saveComponents(components: List<String>) {
        store.put(KEY_COMPONENTS, PinnedAppsCodec.encode(components))
        store.put(KEY_INITIALIZED, "1")
    }

    companion object {
        private const val KEY_COMPONENTS = "pinned_components"
        private const val KEY_INITIALIZED = "favorites_initialized"
    }
}

object InitialFavorites {
    private val preferredPackages = listOf(
        "com.google.android.youtube.tv",
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "org.videolan.vlc",
        "org.jellyfin.androidtv",
        "com.google.android.videos",
        "com.suntv.sunnxt",
        "in.startv.hotstar",
    )

    fun choose(available: List<LauncherApp>, limit: Int = 8): List<LauncherApp> {
        val rank = preferredPackages.withIndex().associate { it.value to it.index }
        return available.sortedWith(
            compareBy<LauncherApp> { rank[it.packageName] ?: Int.MAX_VALUE }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.label }
        ).take(limit)
    }
}
