package com.livianne.autoclicker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private val targetViews = mutableListOf<View>()
    private val targetPositions = mutableListOf<Pair<Int, Int>>()
    private var playPauseView: View? = null

    private var clickSpeed: Long = 1000L
    private var isRunning = false
    private var clickJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        clickSpeed = intent?.getLongExtra("speed", 1000L) ?: 1000L
        val targetCount = intent?.getIntExtra("targetCount", 1) ?: 1

        // Remove old views if service restarted
        clearViews()

        // Create targets
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        for (i in 0 until targetCount) {
            addTarget(
                x = screenWidth / 2 - 60,
                y = screenHeight / 2 - 60 + i * 150,
                label = "${i + 1}"
            )
        }

        addPlayPauseButton()

        return START_STICKY
    }

    private fun addTarget(x: Int, y: Int, label: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.floating_target, null)
        val labelView = view.findViewById<TextView>(R.id.targetLabel)
        labelView.text = label

        val params = WindowManager.LayoutParams(
            120, 120,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        var initialX = 0; var initialY = 0
        var touchX = 0f; var touchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    touchX = event.rawX; touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(v, params)
                    val idx = targetViews.indexOf(v)
                    if (idx >= 0) targetPositions[idx] = Pair(params.x + 60, params.y + 60)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(view, params)
        targetViews.add(view)
        targetPositions.add(Pair(x + 60, y + 60))
    }

    private fun addPlayPauseButton() {
        val view = LayoutInflater.from(this).inflate(R.layout.floating_playpause, null)
        val btn = view.findViewById<ImageButton>(R.id.btnPlayPause)

        val params = WindowManager.LayoutParams(
            90, 90,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = resources.displayMetrics.heightPixels / 2
        }

        var initialY = 0; var touchY = 0f; var moved = false

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = params.y; touchY = event.rawY; moved = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = (event.rawY - touchY).toInt()
                    if (Math.abs(dy) > 8) moved = true
                    params.y = initialY + dy
                    windowManager.updateViewLayout(v, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) togglePlayPause(btn)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(view, params)
        playPauseView = view
    }

    private fun togglePlayPause(btn: ImageButton) {
        isRunning = !isRunning
        if (isRunning) {
            btn.setImageResource(android.R.drawable.ic_media_pause)
            startClicking()
        } else {
            btn.setImageResource(android.R.drawable.ic_media_play)
            clickJob?.cancel()
        }
    }

    private fun startClicking() {
        clickJob?.cancel()
        clickJob = serviceScope.launch {
            var index = 0
            while (isRunning) {
                if (targetPositions.isNotEmpty()) {
                    val pos = targetPositions[index % targetPositions.size]
                    AutoClickAccessibilityService.instance?.performClick(pos.first.toFloat(), pos.second.toFloat())
                    index++
                }
                delay(clickSpeed)
            }
        }
    }

    private fun clearViews() {
        targetViews.forEach { runCatching { windowManager.removeView(it) } }
        targetViews.clear()
        targetPositions.clear()
        playPauseView?.let { runCatching { windowManager.removeView(it) } }
        playPauseView = null
        clickJob?.cancel()
        isRunning = false
    }

    override fun onDestroy() {
        clearViews()
        serviceScope.cancel()
        super.onDestroy()
    }
}
