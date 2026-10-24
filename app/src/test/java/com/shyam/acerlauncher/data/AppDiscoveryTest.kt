package com.shyam.acerlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDiscoveryTest {
    @Test
    fun catalogIsDeduplicatedAndSortedByLabel() {
        val beta = LauncherApp("b/.Home", "b", "Beta")
        val alpha = LauncherApp("a/.Home", "a", "alpha")

        assertEquals(listOf(alpha, beta), AppDiscovery.deduplicateAndSort(listOf(beta, alpha, beta)))
    }
}
