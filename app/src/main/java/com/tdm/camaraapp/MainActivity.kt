package com.tdm.camaraapp

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: ImageButton
    private lateinit var frozenImage: ImageView
    private lateinit var permissionHelper: PermissionHelper

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas y PermissionHelper
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        frozenImage = findViewById(R.id.frozenImage)
        permissionHelper = PermissionHelper(this)

        // Verifica si el permiso de cámara ha sido concedido
        if (permissionHelper.isPermissionGranted(Manifest.permission.CAMERA)) {
            startCamera()
        } else {
            permissionHelper.requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE)
        }

        btnCapture.setOnClickListener {
            Log.d("MainActivity", "Capturando imagen...")

            captureImage { bitmap ->
                Log.d("MainActivity", "Imagen capturada con éxito")

                // Mostrar el Bitmap en el ImageView
                frozenImage.setImageBitmap(bitmap)

                // Ocultar la cámara y mostrar la imagen congelada
                frozenImage.visibility = View.VISIBLE
                previewView.visibility = View.GONE
                Log.d("MainActivity", "Vista de la cámara oculta, imagen congelada visible")
            }
        }

        // Lógica de la animación del botón
        val buttonAnimator = ButtonAnimator()
        buttonAnimator.setButtonAnimation(btnCapture)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage(onImageCaptured: (Bitmap) -> Unit) {
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                onImageCaptured(bitmap)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionHelper.handlePermissionResult(
            requestCode = requestCode,
            expectedRequestCode = CAMERA_PERMISSION_REQUEST_CODE,
            grantResults = grantResults,
            onGranted = { startCamera() },
            onDenied = {
                Toast.makeText(this, "El permiso de cámara es necesario para usar la aplicación.", Toast.LENGTH_LONG).show()
            }
        )
    }
}
