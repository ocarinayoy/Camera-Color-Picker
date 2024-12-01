package com.tdm.camaraapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

        frozenImage.setOnTouchListener { view, event ->

            Toast.makeText(this, "tocado", Toast.LENGTH_SHORT).show()
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val x = event.x.toInt()
                val y = event.y.toInt()

                // Obtener el bitmap de la vista de la cámara
                val bitmap = getBitmapFromPreview(previewView)

                // Obtener el color en las coordenadas tocadas
                if (bitmap != null && x < bitmap.width && y < bitmap.height) {
                    val pixelColor = bitmap.getPixel(x, y)
                    updateColorInfo(pixelColor)
                }
            }
            true
        }


    }

    private fun getBitmapFromPreview(previewView: PreviewView): Bitmap? {
        try {
            val bitmap = Bitmap.createBitmap(previewView.width, previewView.height, Bitmap.Config.ARGB_8888)
            previewView.draw(Canvas(bitmap))
            return bitmap
        } catch (e: Exception) {
            Log.e("CameraApp", "Error al obtener el bitmap: ${e.message}")
            return null
        }
    }

    private fun updateColorInfo(color: Int) {
        // Obtener el valor RGB del color
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        // Convertir a formato HEX
        val hexColor = String.format("#%02X%02X%02X", red, green, blue)

        // Actualizar los TextViews
        findViewById<TextView>(R.id.tvHexValue).text = "HEX: $hexColor"
        findViewById<TextView>(R.id.tvRGBValue).text = "RGB: $red, $green, $blue"

        // Puedes agregar una lógica para mostrar el nombre del color si es necesario
        val colorName = getColorName(red, green, blue)
        findViewById<TextView>(R.id.tvColorName).text = "Color: $colorName"
    }

    // Función para obtener el nombre del color (opcional)
    private fun getColorName(red: Int, green: Int, blue: Int): String {
        // Aquí puedes agregar una lógica para mapear el color a un nombre específico.
        // Este es solo un ejemplo básico.
        return when {
            red > green && red > blue -> "Rojo"
            green > red && green > blue -> "Verde"
            blue > red && blue > green -> "Azul"
            else -> "Desconocido"
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

