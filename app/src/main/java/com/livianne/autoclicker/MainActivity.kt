package com.livianne.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val speedInput = findViewById<EditText>(R.id.speedInput)
        val targetCountInput = findViewById<EditText>(R.id.targetCountInput)
        val btnStartService = findViewById<Button>(R.id.btnStartService)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val statusText = findViewById<TextView>(R.id.statusText)

        updateStatus(statusText)

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        btnStartService.setOnClickListener {
            val speed = speedInput.text.toString().toLongOrNull() ?: 1000L
            val count = targetCountInput.text.toString().toIntOrNull() ?: 1

            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Autorisez l'affichage par-dessus les autres apps d'abord", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!isAccessibilityEnabled()) {
                Toast.makeText(this, "Activez le service d'accessibilité AutoClicker d'abord", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val intent = Intent(this, FloatingService::class.java).apply {
                putExtra("speed", speed)
                putExtra("targetCount", count)
            }
            startService(intent)
            Toast.makeText(this, "Service démarré — les cibles apparaissent sur l'écran", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.statusText)
        updateStatus(statusText)
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "${packageName}/${AutoClickAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(service)
    }

    private fun updateStatus(statusText: TextView) {
        val overlayOk = Settings.canDrawOverlays(this)
        val accessOk = isAccessibilityEnabled()
        statusText.text = buildString {
            append("Affichage par-dessus les apps : ${if (overlayOk) "✅" else "❌"}\n")
            append("Service d'accessibilité : ${if (accessOk) "✅" else "❌"}")
        }
    }
}
