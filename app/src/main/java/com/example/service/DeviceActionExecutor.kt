package com.example.service

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

object DeviceActionExecutor {

    private var isTorchOn = false

    fun launchYouTube(context: Context, query: String? = null) {
        try {
            val intent = if (!query.isNullOrBlank()) {
                Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    setPackage("com.google.android.youtube")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to browser
                val webUrl = if (!query.isNullOrBlank()) {
                    "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
                } else {
                    "https://www.youtube.com"
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "یوٹیوب کھولنے میں خرابی: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchMaps(context: Context, destination: String) {
        try {
            val encodedDestination = Uri.encode(destination)
            val gmmIntentUri = Uri.parse("google.navigation:q=$encodedDestination")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webMaps = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webMaps)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "نقشہ کھولنے میں خرابی: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchDialer(context: Context, phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "ڈائلر کھولنے میں خرابی: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchWhatsApp(context: Context, phoneNumber: String? = null, message: String? = null) {
        try {
            val url = if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
                val textParam = if (!message.isNullOrBlank()) "&text=${Uri.encode(message)}" else ""
                "https://api.whatsapp.com/send?phone=$cleanPhone$textParam"
            } else {
                "https://api.whatsapp.com"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "واٹس ایپ کھولنے میں خرابی: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera(context: Context) {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "کیمرہ کھولنے میں خرابی: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFlashlight(context: Context): Boolean {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return false
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return false

            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            Toast.makeText(
                context,
                if (isTorchOn) "💡 فلیش لائٹ آن ہو گئی ہے" else "🌑 فلیش لائٹ بند ہو گئی ہے",
                Toast.LENGTH_SHORT
            ).show()
            return isTorchOn
        } catch (e: CameraAccessException) {
            Toast.makeText(context, "فلیش لائٹ میں خرابی", Toast.LENGTH_SHORT).show()
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun isFlashlightActive(): Boolean = isTorchOn
}
