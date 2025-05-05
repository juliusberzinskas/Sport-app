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

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.lang.reflect.Type
import java.util.*

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

    private lateinit var sharedPrefs: SharedPreferences
    private val gson = Gson()
    private val historyKey = "workout_history"

    // Timeris kas sekundę atnaujina laiką ir statistiką
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                secondsElapsed++
                updateTimerUI()
                updateStats()
            } else {
                tvSpeed.text = "--"
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPrefs = getSharedPreferences("workout_data", Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_page)

        timerView = findViewById(R.id.tvTimer)
        btnPause = findViewById(R.id.btnPause)
        btnResume = findViewById(R.id.btnResume)
        btnStop = findViewById(R.id.btnStop)
        tvSteps = findViewById(R.id.workout_Steps)
        tvCalories = findViewById(R.id.workout_Calories)
        tvDistance = findViewById(R.id.workout_Distance)
        tvSpeed = findViewById(R.id.workout_Speed)

        btnPause.setOnClickListener {
            isRunning = false
            isPaused = true
            btnPause.visibility = View.GONE
            btnResume.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }

        btnResume.setOnClickListener {
            isRunning = true
            isPaused = false
            btnPause.visibility = View.VISIBLE
            btnResume.visibility = View.GONE
            btnStop.visibility = View.GONE
        }

        btnStop.setOnClickListener {
            isRunning = false
            saveWorkoutSession()
            finish()
        }

        setupStepSensor()
        handler.post(timerRunnable)
    }

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

    private fun updateTimerUI() {
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val seconds = secondsElapsed % 60
        timerView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun updateStats() {
        val distance = stepCount * userStepLengthM / 1000
        val timeHours = secondsElapsed / 3600.0
        val speed = if (timeHours > 0) distance / timeHours else 0.0
        val calories = 0.04 * stepCount

        tvSteps.text = stepCount.toString()
        tvDistance.text = String.format("%.2f", distance)
        tvCalories.text = calories.roundToInt().toString()

        if (isRunning) {
            tvSpeed.text = String.format("%.1f", speed)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        sensorManager.unregisterListener(this)
    }

    private fun saveWorkoutSession() {
        val session = WorkoutSession(
            type = "bėgimas",
            dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            steps = stepCount,
            distanceKm = String.format("%.2f", stepCount * userStepLengthM / 1000).replace(",", ".").toDouble(),
            durationSec = secondsElapsed,
            calories = (0.04 * stepCount).roundToInt()
        )

        val existingJson = sharedPrefs.getString(historyKey, null)
        val type: Type = object : TypeToken<MutableList<WorkoutSession>>() {}.type

        val sessions: MutableList<WorkoutSession> = if (existingJson != null) {
            gson.fromJson(existingJson, type)
        } else {
            mutableListOf()
        }

        sessions.add(session)
        val updatedJson = gson.toJson(sessions)
        sharedPrefs.edit().putString(historyKey, updatedJson).apply()
        println("✅ Treniruotė išsaugota!")
    }

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
