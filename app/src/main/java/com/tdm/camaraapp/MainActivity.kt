package com.tdm.camaraapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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

        btnCapture.setOnClickListener {
            // Limpiar la imagen previa antes de capturar una nueva
            frozenImage.setImageDrawable(null)
            frozenImage.visibility = View.GONE

            Toast.makeText(this, "Capturando...", Toast.LENGTH_SHORT).show()
            cameraManager.captureImage(
                executor = ContextCompat.getMainExecutor(this),
                onImageCaptured = { uri ->
                    Log.d("MainActivity", "Imagen capturada: $uri")

                    // Actualizar la imagen congelada con la nueva URI
                    frozenImage.setImageURI(uri)
                    frozenImage.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                },
                onError = { exception ->
                    Log.e("MainActivity", "Error al capturar la imagen: ${exception.message}")
                }
            )
        }


        frozenImage.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val imageViewWidth = frozenImage.width.toFloat()
                val imageViewHeight = frozenImage.height.toFloat()

                val drawable = frozenImage.drawable as? BitmapDrawable
                val bitmap = drawable?.bitmap

                if (bitmap != null) {
                    val bitmapWidth = bitmap.width.toFloat()
                    val bitmapHeight = bitmap.height.toFloat()

                    // Coordenadas táctiles relativas al ImageView
                    val xTouch = event.x
                    val yTouch = event.y

                    // Escalar las coordenadas táctiles al tamaño del Bitmap
                    val x = ((xTouch / imageViewWidth) * bitmapWidth).toInt()
                    val y = ((yTouch / imageViewHeight) * bitmapHeight).toInt()

                    // Verificar si las coordenadas están dentro de los límites del Bitmap
                    if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
                        val pixelColor = bitmap.getPixel(x, y)
                        updateColorInfo(pixelColor)
                    }
                }
            }
            true
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
        return when {
            red > green && red > blue -> "Rojo"
            green > red && green > blue -> "Verde"
            blue > red && blue > green -> "Azul"
            else -> "Desconocido"
        }
    }

    override fun onBackPressed() {
        if (frozenImage.visibility == View.VISIBLE) {
            // Ocultar la imagen congelada y reiniciar la vista previa
            frozenImage.setImageDrawable(null)
            frozenImage.visibility = View.GONE
            previewView.visibility = View.VISIBLE

            // Reiniciar la cámara
            initializeCamera()
        } else {
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
