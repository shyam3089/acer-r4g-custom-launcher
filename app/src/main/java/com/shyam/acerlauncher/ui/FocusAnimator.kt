package com.shyam.acerlauncher.ui

import android.animation.ValueAnimator
import android.view.View

object FocusAnimator {
    fun attach(view: View, focusedScale: Float = 1.06f) {
        view.isSoundEffectsEnabled = false
        NavigationSound.prepare(view.context)
        view.setOnFocusChangeListener { target, focused ->
            target.animate().cancel()
            if (focused) NavigationSound.play(target.context)
            val scale = if (focused) focusedScale else 1f
            val elevation = if (focused) 18f else 4f
            if (ValueAnimator.areAnimatorsEnabled()) {
                target.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationZ(elevation)
                    .setDuration(150L)
                    .start()
            } else {
                target.scaleX = scale
                target.scaleY = scale
                target.translationZ = elevation
            }
        }
    }
}
