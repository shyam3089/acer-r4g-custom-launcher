package com.shyam.acerlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PinnedAppsCodecTest {
    @Test
    fun roundTripPreservesOrderAndRemovesDuplicates() {
        val values = listOf("one/.Main", "two/.Home", "one/.Main")

        assertEquals(listOf("one/.Main", "two/.Home"), PinnedAppsCodec.decode(PinnedAppsCodec.encode(values)))
    }

    @Test
    fun blankStorageProducesEmptyList() {
        assertEquals(emptyList<String>(), PinnedAppsCodec.decode(null))
        assertEquals(emptyList<String>(), PinnedAppsCodec.decode("\n  \n"))
    }
}
