package com.shyam.acerlauncher.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.shyam.acerlauncher.R

object NavigationSound {
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var ready = false

    @Synchronized
    fun prepare(context: Context) {
        if (soundPool != null) return
        val pool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
        soundPool = pool
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == soundId && status == 0) ready = true
        }
        soundId = pool.load(context.applicationContext, R.raw.navigation_ding, 1)
    }

    fun play(context: Context) {
        prepare(context)
        if (ready) soundPool?.play(soundId, 0.22f, 0.22f, 1, 0, 1f)
    }
}
