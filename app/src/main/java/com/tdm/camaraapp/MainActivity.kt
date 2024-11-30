package com.tdm.camaraapp

import android.Manifest
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

    // Variables para vistas y utilidades
    private lateinit var previewView: PreviewView // Vista donde se muestra la cámara en tiempo real
    private lateinit var btnCapture: ImageButton // Botón para capturar la imagen
    private lateinit var frozenImage: ImageView // Vista para mostrar la imagen capturada
    private lateinit var permissionHelper: PermissionHelper // Helper para manejar permisos de usuario
    private lateinit var cameraManager: CameraManager // Administrador de la cámara

    // Código para la solicitud de permisos de cámara
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos las vistas y las clases auxiliares
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        frozenImage = findViewById(R.id.frozenImage)
        permissionHelper = PermissionHelper(this)
        cameraManager = CameraManager(this)

        // Verificamos si los permisos de cámara ya están concedidos
        if (permissionHelper.isPermissionGranted(Manifest.permission.CAMERA)) {
            // Si los permisos están concedidos, inicializamos la cámara
            initializeCamera()
        } else {
            // Si no, solicitamos el permiso al usuario
            permissionHelper.requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE)
        }

        btnCapture.setOnClickListener {
            Toast.makeText(this, "pressed", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "Botón de captura presionado")
            cameraManager.captureImage(
                executor = ContextCompat.getMainExecutor(this),
                onImageCaptured = { bitmap ->
                    Log.d("MainActivity", "Imagen capturada correctamente")
                    frozenImage.setImageBitmap(bitmap)
                    frozenImage.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                },
                onError = { exception ->
                    Log.e("MainActivity", "Error al capturar imagen: ${exception.message}")
                }
            )
        }


        // Agregamos animación al botón de captura
        val buttonAnimator = ButtonAnimator()
        buttonAnimator.setButtonAnimation(btnCapture)
    }

    // Inicializa la cámara utilizando el CameraManager
    private fun initializeCamera() {
        cameraManager.startCamera(previewView) {
            // Mostramos un mensaje cuando la cámara esté lista
            Toast.makeText(this, "Cámara inicializada", Toast.LENGTH_SHORT).show()
        }
    }

    // Manejo de los resultados de las solicitudes de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Utilizamos el PermissionHelper para procesar la respuesta del usuario
        permissionHelper.handlePermissionResult(
            requestCode = requestCode,
            expectedRequestCode = CAMERA_PERMISSION_REQUEST_CODE,
            grantResults = grantResults,
            onGranted = {
                // Si el permiso es concedido, inicializamos la cámara
                initializeCamera()
            },
            onDenied = {
                // Si el permiso es denegado, mostramos un mensaje al usuario
                Toast.makeText(this, "El permiso de cámara es necesario para usar la aplicación.", Toast.LENGTH_LONG).show()
            }
        )
    }
}
