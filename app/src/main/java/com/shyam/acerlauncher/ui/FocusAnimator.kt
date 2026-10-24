package com.shyam.acerlauncher.ui

import android.animation.ValueAnimator
import android.view.View

object FocusAnimator {
    fun attach(view: View) {
        view.setOnFocusChangeListener { target, focused ->
            target.animate().cancel()
            val scale = if (focused) 1.06f else 1f
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
