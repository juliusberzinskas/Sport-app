// WorkoutActivity.kt
package com.example.sportoapppit

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class WorkoutActivity : AppCompatActivity(), SensorEventListener {

    // UI komponentai
    private lateinit var timerView: TextView
    private lateinit var btnPause: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var tvSteps: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView

    // Treniruotės kintamieji
    private var secondsElapsed = 0
    private var isRunning = true
    private var isPaused = false

    private val handler = Handler(Looper.getMainLooper())

    private val userWeightKg = 70
    private val userStepLengthM = 0.7

    private var stepCount = 0
    private var startSteps = -1

    private lateinit var sensorManager: SensorManager

    // Timeris kas sekundę atnaujina laiką ir statistiką
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                secondsElapsed++
                updateTimerUI()
                updateStats()
            } else {
                // jei pauzė – tempą nustatom į --
                tvSpeed.text = "--"
            }
            handler.postDelayed(this, 1000)
        }
    }

    // paleidžiama kai atidarom puslapį
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_page)

        // susiejame UI komponentus
        timerView = findViewById(R.id.tvTimer)
        btnPause = findViewById(R.id.btnPause)
        btnResume = findViewById(R.id.btnResume)
        btnStop = findViewById(R.id.btnStop)
        tvSteps = findViewById(R.id.workout_Steps)
        tvCalories = findViewById(R.id.workout_Calories)
        tvDistance = findViewById(R.id.workout_Distance)
        tvSpeed = findViewById(R.id.workout_Speed)

        // pauzės logika
        btnPause.setOnClickListener {
            isRunning = false
            isPaused = true
            btnPause.visibility = View.GONE
            btnResume.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }

        // tęsti logika
        btnResume.setOnClickListener {
            isRunning = true
            isPaused = false
            btnPause.visibility = View.VISIBLE
            btnResume.visibility = View.GONE
            btnStop.visibility = View.GONE
        }

        // sustabdyti treniruotę
        btnStop.setOnClickListener {
            isRunning = false
            finish()
        }

        setupStepSensor()
        handler.post(timerRunnable)
    }

    // prijungiam žingsnių jutiklį
    private fun setupStepSensor() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                2001
            )
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // gaunam žingsnių atnaujinimus
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && !isPaused) {
            val totalSteps = event.values[0].toInt()
            if (startSteps == -1) {
                startSteps = totalSteps
            }
            stepCount = totalSteps - startSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // atnaujina laikmatį
    private fun updateTimerUI() {
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val seconds = secondsElapsed % 60
        timerView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Apskaičiuoja visus rodiklius
    private fun updateStats() {
        val distance = stepCount * userStepLengthM / 1000  // km
        val timeHours = secondsElapsed / 3600.0
        val speed = if (timeHours > 0) distance / timeHours else 0.0
        val calories = 0.04 * stepCount

        tvSteps.text = stepCount.toString()
        tvDistance.text = String.format("%.2f", distance)
        tvCalories.text = calories.roundToInt().toString()

        // tempą rodome tik jei veikia
        if (isRunning) {
            tvSpeed.text = String.format("%.1f", speed)
        }
    }

    // kai puslapis sunaikinamas
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        sensorManager.unregisterListener(this)
    }

    // kai leidimai suteikiami
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupStepSensor()
        }
    }
}
