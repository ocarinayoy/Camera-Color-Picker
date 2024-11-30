package com.tdm.camaraapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import java.util.concurrent.Executor

class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null

    fun startCamera(previewView: PreviewView, onCameraReady: () -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configuración de Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Configuración de ImageCapture
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

                // Obtener la rotación de la cámara
                val rotationDegrees = getRotationDegrees()
                onCameraReady()

            } catch (e: Exception) {
                Log.e("CameraManager", "Error al iniciar la cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun captureImage(executor: Executor, onImageCaptured: (Bitmap) -> Unit, onError: (Exception) -> Unit) {
        val imageCapture = this.imageCapture
        if (imageCapture == null) {
            Log.e("CameraManager", "ImageCapture no inicializado")
            onError(Exception("ImageCapture no inicializado"))
            return
        }

        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                Log.d("CameraManager", "Captura exitosa")
                val bitmap = imageProxyToBitmap(image)
                onImageCaptured(bitmap)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraManager", "Error al capturar imagen: ${exception.message}")
                onError(exception)
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        // Convertimos la imagen a un Bitmap
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Obtener la rotación correcta de la cámara
        val rotationDegrees = getRotationDegrees()

        // Rotamos el Bitmap si es necesario
        return rotateBitmap(bitmap, rotationDegrees)
    }

    // Obtener la rotación de la cámara en función de la orientación de la pantalla
    private fun getRotationDegrees(): Int {
        val rotation = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayRotation = rotation.defaultDisplay.rotation

        // Aseguramos que la rotación se calcule en grados, dependiendo de la orientación
        return when (displayRotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    // Función para rotar el Bitmap
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
