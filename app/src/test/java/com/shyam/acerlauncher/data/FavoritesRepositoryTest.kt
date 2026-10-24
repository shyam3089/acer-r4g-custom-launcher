package com.shyam.acerlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesRepositoryTest {
    private val youtube = app("youtube/.Home", "com.google.android.youtube.tv", "YouTube")
    private val netflix = app("netflix/.Home", "com.netflix.ninja", "Netflix")
    private val other = app("other/.Home", "dev.example.other", "Other")

    @Test
    fun firstLoadPinsUsefulInstalledAppsOnly() {
        val repository = FavoritesRepository(FakeStore())

        assertEquals(listOf(youtube, netflix, other), repository.loadOrInitialize(listOf(other, netflix, youtube)))
    }

    @Test
    fun laterLoadRemovesUninstalledAppsWithoutRepopulatingEmptyChoice() {
        val store = FakeStore()
        val repository = FavoritesRepository(store)
        repository.loadOrInitialize(listOf(youtube, netflix))
        repository.toggle(youtube.component)
        repository.toggle(netflix.component)

        assertEquals(emptyList<LauncherApp>(), repository.loadOrInitialize(listOf(youtube)))
    }

    @Test
    fun toggleAndMovePersistOrderedComponents() {
        val store = FakeStore()
        val repository = FavoritesRepository(store)
        repository.loadOrInitialize(listOf(youtube, netflix, other))

        assertFalse(repository.toggle(netflix.component))
        assertTrue(repository.toggle(netflix.component))
        assertEquals(
            listOf(netflix.component, youtube.component, other.component),
            repository.move(2, 0),
        )
    }

    private fun app(component: String, packageName: String, label: String) =
        LauncherApp(component, packageName, label)

    private class FakeStore : StringStore {
        private val values = mutableMapOf<String, String>()
        override fun get(key: String): String? = values[key]
        override fun put(key: String, value: String) {
            values[key] = value
        }
    }
}
