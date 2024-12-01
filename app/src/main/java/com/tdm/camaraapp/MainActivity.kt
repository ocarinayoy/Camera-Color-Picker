package com.tdm.camaraapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var frozenImage: ImageView
    private lateinit var btnCapture: ImageButton
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        frozenImage = findViewById(R.id.frozenImage)
        btnCapture = findViewById(R.id.btnCapture)

        cameraManager = CameraManager(this)

        // Verificamos los permisos de cámara
        val permissionHelper = PermissionHelper(this)
        if (permissionHelper.isPermissionGranted(Manifest.permission.CAMERA)) {
            initializeCamera()
        } else {
            permissionHelper.requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE)
        }


//        Tomar la foto
        btnCapture.setOnClickListener {
            Toast.makeText(this, "capturando...", Toast.LENGTH_SHORT).show()
            cameraManager.captureImage(
                executor = ContextCompat.getMainExecutor(this),
                onImageCaptured = { uri ->
                    Log.d("MainActivity", "Imagen capturada: $uri")
                    frozenImage.setImageURI(uri)  // Mostramos la imagen en el ImageView
                    frozenImage.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                },
                onError = { exception ->
                    Log.e("MainActivity", "Error al capturar la imagen: ${exception.message}")
                }
            )
        }
    }

    override fun onBackPressed() {
        // Verificar si la imagen congelada está visible
        if (frozenImage.visibility == View.VISIBLE) {
            // Ocultar la imagen congelada
            frozenImage.visibility = View.GONE
            // Mostrar la vista de la cámara nuevamente
            previewView.visibility = View.VISIBLE

            // Reiniciar la cámara
            initializeCamera()
        } else {
            // Si no hay imagen congelada, ejecutar el comportamiento estándar de retroceso
            super.onBackPressed()
        }
    }


    private fun initializeCamera() {
        cameraManager.startCamera(previewView) {
            Toast.makeText(this, "Cámara inicializada", Toast.LENGTH_SHORT).show()
        }
    }

    // Manejo de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara necesario", Toast.LENGTH_LONG).show()
        }
    }
}

