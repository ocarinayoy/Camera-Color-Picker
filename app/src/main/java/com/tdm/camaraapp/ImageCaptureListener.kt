package com.tdm.camaraapp

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class ImageCaptureListener(
    private val onImageCaptured: (Bitmap) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageCapture.OnImageCapturedCallback() {

    override fun onCaptureSuccess(image: ImageProxy) {
        val bitmap = imageProxyToBitmap(image)
        onImageCaptured(bitmap)
        image.close() // Liberar el recurso
    }

    override fun onError(exception: ImageCaptureException) {
        onError(exception)
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
