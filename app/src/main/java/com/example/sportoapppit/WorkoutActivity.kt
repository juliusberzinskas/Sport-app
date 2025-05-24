// WorkoutActivity.kt
package com.example.sportoapppit

import WorkoutSession
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import java.util.Date
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.lang.reflect.Type
import java.util.*

import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import android.graphics.Color
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WorkoutActivity : AppCompatActivity(), SensorEventListener, OnMapReadyCallback {

    private lateinit var timerView: TextView
    private lateinit var btnPause: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var tvSteps: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView

    private var secondsElapsed = 0
    private var isRunning = true
    private var isPaused = false

    private val handler = Handler(Looper.getMainLooper())
    private val userStepLengthM = 0.7
    private var stepCount = 0
    private var startSteps = -1
    private var currentSteps = 0

    private lateinit var sensorManager: SensorManager
    private lateinit var sharedPrefs: SharedPreferences
    private val gson = Gson()
    private val historyKey = "workout_history"

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: LatLng? = null
    private val polylinePoints = mutableListOf<LatLng>()
    private var locationDistanceMeters = 0.0

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
            saveMapImageAndSession()
            finish()
        }

        setupStepSensor()
        handler.post(timerRunnable)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.workoutMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        enableLocationTracking()
    }

    private fun enableLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1002)
            return
        }

        try {
            map.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                lastLocation?.let {
                    val distance = floatArrayOf(0f)
                    android.location.Location.distanceBetween(
                        it.latitude, it.longitude,
                        latLng.latitude, latLng.longitude,
                        distance
                    )
                    locationDistanceMeters += distance[0]
                }

                if (lastLocation == null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                }

                polylinePoints.add(latLng)
                drawPolyline()
                lastLocation = latLng
            }
        }, Looper.getMainLooper())
    }

    private fun drawPolyline() {
        map.addPolyline(
            PolylineOptions()
                .color(Color.CYAN)
                .width(8f)
                .addAll(polylinePoints)
        )
    }

    private fun saveMapImageAndSession() {
        try {
            map.isMyLocationEnabled = false // laikinai išjungiam rodymą
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            map.snapshot { bitmap ->
                bitmap?.let {
                    val fileName = "map_${System.currentTimeMillis()}.png"
                    val file = File(cacheDir, fileName)
                    FileOutputStream(file).use { out ->
                        it.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                    }
                    saveWorkoutSession(file.absolutePath)
                } ?: run {
                    saveWorkoutSession("")
                }
            }
        }, 300)
    }

    private fun setupStepSensor() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 2001)
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
        val useGps = lastLocation != null &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val distanceKm = if (useGps) locationDistanceMeters / 1000 else stepCount * userStepLengthM / 1000
        val timeHours = secondsElapsed / 3600.0
        val speed = if (timeHours > 0) distanceKm / timeHours else 0.0


        val mets = 6.0 // bėgimui ~6.0 MET, vaikščiojimui ~3.5
        val weightKg = 70
        val durationMin = secondsElapsed / 60.0
        val calories = (mets * 3.5 * weightKg / 200) * durationMin

        tvSteps.text = stepCount.toString()
        tvDistance.text = String.format("%.2f", distanceKm)
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

    private fun saveWorkoutSession(mapPath: String) {
        val useGps = lastLocation != null &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val distanceKm = if (useGps) locationDistanceMeters / 1000 else stepCount * userStepLengthM / 1000
        val durationSec = secondsElapsed
        val durationMin = durationSec / 60.0
        val mets = 6.0
        val weightKg = 70
        val calories = (mets * 3.5 * weightKg / 200) * durationMin
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val workoutSession = WorkoutSession(
            type = "bėgimas", // or "ėjimas"
            dateTime = formattedDate,
            steps = stepCount,
            distanceKm = distanceKm,
            durationSec = durationSec,
            calories = calories,
            mapImagePath = mapPath
        )

        // Save locally
        val existingJson = sharedPrefs.getString(historyKey, null)
        val type: Type = object : TypeToken<MutableList<WorkoutSession>>() {}.type
        val sessions: MutableList<WorkoutSession> = if (existingJson != null) {
            gson.fromJson(existingJson, type)
        } else {
            mutableListOf()
        }
        sessions.add(workoutSession)
        val updatedJson = gson.toJson(sessions)
        sharedPrefs.edit() { putString(historyKey, updatedJson) }

        // Save to Firestore
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid)
                .collection("workouts")
                .add(workoutSession)
                .addOnSuccessListener {
                    Toast.makeText(this, "Treniruotė išsaugota!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Nepavyko išsaugoti: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            val userAge = UserPreferences.getUserAge(this)
            val userWeight = UserPreferences.getUserWeight(this)
            val userHeight = UserPreferences.getUserHeight(this)
            val userGender = UserPreferences.getUserGender(this) // If you track this

            val bmi = userWeight / ((userHeight / 100.0) * (userHeight / 100.0))
            val fatPercent = estimateFatPercentage(userWeight, userHeight, userAge, userGender)

            val stat = ProfileStats.ProfileStat(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                weight = userWeight,
                height = userHeight,
                bmi = bmi,
                fatPercent = fatPercent
            )

            db.collection("users").document(user.uid)
                .collection("stats")
                .add(stat)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupStepSensor()
        } else if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationTracking()
        }
    }

    fun estimateFatPercentage(weight: Double, height: Double, age: Int, gender: String): Double {
        val bmi = weight / ((height / 100) * (height / 100))
        return if (gender.lowercase() == "male") {
            (1.20 * bmi) + (0.23 * age) - 16.2
        } else {
            (1.20 * bmi) + (0.23 * age) - 5.4
        }
    }

}
