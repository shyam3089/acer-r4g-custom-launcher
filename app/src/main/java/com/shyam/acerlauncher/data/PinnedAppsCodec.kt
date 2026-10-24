package com.shyam.acerlauncher.data

object PinnedAppsCodec {
    fun encode(components: List<String>): String = components
        .asSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .joinToString("\n")

    fun decode(value: String?): List<String> = value
        .orEmpty()
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .toList()
}
