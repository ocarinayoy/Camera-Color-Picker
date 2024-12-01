package com.tdm.camaraapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executor

class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private val outputDirectory: File = getOutputDirectory()

    fun startCamera(previewView: PreviewView, onCameraReady: () -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                onCameraReady()

            } catch (e: Exception) {
                Log.e("CameraManager", "Error al iniciar la cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun captureImage(executor: Executor, onImageCaptured: (Uri) -> Unit, onError: (Exception) -> Unit) {
        val imageCapture = this.imageCapture ?: run {
            onError(Exception("ImageCapture no inicializado"))
            return
        }

        val photoFile = File(outputDirectory, "captured_image.png")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Log.d("CameraManager", "Imagen guardada en: ${photoFile.absolutePath}")
                onImageCaptured(Uri.fromFile(photoFile))  // Pasamos la URI del archivo
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })
    }

    // Directorio donde guardaremos las fotos
    private fun getOutputDirectory(): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }
}
