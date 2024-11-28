package com.tdm.camaraapp

import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.animation.ObjectAnimator

// Clase encargada de la animación de los botones
class ButtonAnimator {

    // Método para configurar el listener del botón
    fun setButtonAnimation(btnCapture: ImageButton) {
        btnCapture.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Animación para reducir el tamaño
                    ObjectAnimator.ofFloat(view, "scaleX", 0.85f).apply {
                        duration = 100
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                    ObjectAnimator.ofFloat(view, "scaleY", 0.85f).apply {
                        duration = 100
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Animación para volver al tamaño original
                    ObjectAnimator.ofFloat(view, "scaleX", 1f).apply {
                        duration = 100
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                    ObjectAnimator.ofFloat(view, "scaleY", 1f).apply {
                        duration = 100
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                }
            }
            true // Indica que el evento ha sido consumido
        }
    }
}
