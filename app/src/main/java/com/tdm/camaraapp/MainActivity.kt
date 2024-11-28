package com.tdm.camaraapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.camera.view.PreviewView
import androidx.core.view.WindowInsetsCompat
import androidx.activity.result.ActivityResultLauncher

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: ImageButton

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)

        // Verifica si el permiso de cámara ha sido concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // El permiso ya está concedido, se puede iniciar la cámara
            startCamera()
        } else {
            // Si el permiso no ha sido concedido, lo solicitamos
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        // Lógica de la animación del botón
        val buttonAnimator = ButtonAnimator()
        buttonAnimator.setButtonAnimation(btnCapture)
    }

    // Método para inicializar la cámara
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configuración del selector de cámara (delantera o trasera)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val preview = Preview.Builder().build()

            // Configurar el preview para la vista previa en vivo
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Vincula la cámara al ciclo de vida del activity
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)

        }, ContextCompat.getMainExecutor(this))
    }

    // Método que maneja la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // El permiso ha sido concedido, inicia la cámara
                    startCamera()
                } else {
                    // El permiso no ha sido concedido, muestra un mensaje al usuario
                    Toast.makeText(this, "El permiso de cámara es necesario para usar la aplicación.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
