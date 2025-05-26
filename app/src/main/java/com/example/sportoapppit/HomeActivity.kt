package com.example.sportoapppit

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

private lateinit var stepCounterService: StepCounterService

class HomeActivity : BaseActivity(), SensorEventListener {

    private var lastStepCount = -1
    private var currentSteps = 0

    private var previousStepCount = -1
    private var activeMinutes = 0
    private val activeHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        setupBottomNavigation()
        loadUserProfileFromFirestore()

        // Prašom leidimo jei jo nėra
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                1001
            )
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            println("Jutiklis TYPE_STEP_COUNTER nepasiekiamas šiame telefone")
        } else {
            println("Jutiklis TYPE_STEP_COUNTER veikia")
        }

        stepCounterService = StepCounterService(this)
        stepCounterService.onStepUpdate = { steps ->
            runOnUiThread {
                currentSteps = steps

                // žingsniai
                findViewById<TextView>(R.id.tvStepsCount).text = steps.toString()

                // kalorijos
                val calories = steps * 0.04
                findViewById<TextView>(R.id.tvCalories).text = calories.toInt().toString()

                // atstumas
                val distance = steps * 0.00078
                findViewById<TextView>(R.id.tvDistance).text = String.format("%.2f", distance)

                // progresas
                val goal = UserPreferences.getStepGoal(this)
                findViewById<TextView>(R.id.tvStepsLabel).text = goal.toString()
                val progress = ((steps.toFloat() / goal) * 100).toInt()
                findViewById<ProgressBar>(R.id.progressSteps).progress = progress
            }
        }

        // Kas 3 sekundes spausdina dabartinius žingsnius į Logcat
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                println("Dabartiniai žingsniai: $currentSteps")
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)

        // --- 1. Data ---
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale("lt"))
        val formattedDate = today.format(formatter)
        findViewById<TextView>(R.id.home_page_date).text = formattedDate

        // --- 2. Kalendorius ---
        val days = mutableListOf<CalendarDay>()
        for (i in -1..5) {
            val date = today.plusDays(i.toLong())
            days.add(
                CalendarDay(
                    date = date,
                    isToday = date == today,
                    hasWorkout = (date.dayOfMonth == 5)
                )
            )
        }

        val calendarRecycler = findViewById<RecyclerView>(R.id.calendarRecycler)
        val adapter = CalendarDayAdapter(days) { selectedDate ->
            println("Pasirinkta data: $selectedDate")
        }

        calendarRecycler.layoutManager = GridLayoutManager(this, 7, RecyclerView.VERTICAL, false)
        calendarRecycler.adapter = adapter

        // --- 3. Šiandienos tikslai ---
        val tvGoalTitle = findViewById<TextView>(R.id.tvGoalTitle)
        val tvGoalDescription = findViewById<TextView>(R.id.tvGoalDescription)
        val btnStartGoal = findViewById<Button>(R.id.home_page_btnStartGoal)
        val goalImage = findViewById<ImageView>(R.id.ivGoalImage)

        val plannedToday = "bėgimas"

        if (plannedToday != null) {
            when (plannedToday) {
                "bėgimas" -> {
                    tvGoalTitle.text = "Nubėgti 3 km."
                    tvGoalDescription.text = "Bėgimas stiprina širdį ir mažina streso lygį."
                    goalImage.setImageResource(R.drawable.ic_run)
                }
                "ėjimas" -> {
                    tvGoalTitle.text = "Eiti 5 km."
                    tvGoalDescription.text = "Vaikščiojimas mažina nuovargį ir stiprina ištvermę."
                    goalImage.setImageResource(R.drawable.ic_walk)
                }
            }

           // btnStartGoal.setOnClickListener {
          //      println("Pradedama treniruotė: $plannedToday")}

            //---laikinai, kol tikrinu kaip veikia workout_page
            btnStartGoal.setOnClickListener {
                val intent = Intent(this, WorkoutActivity::class.java)
                startActivity(intent)
            }


        } else {
            findViewById<View>(R.id.goalContainer).visibility = View.GONE
        }


        stepCounterService.start()

        // Kas 60 sekundžių tikrina ar juda
        val activeRunnable = object : Runnable {
            override fun run() {
                if (currentSteps > previousStepCount) {
                    activeMinutes++
                    findViewById<TextView>(R.id.tvTime).text = activeMinutes.toString()
                    previousStepCount = currentSteps
                    println("🕒 Aktyvus: $activeMinutes min")
                }
                activeHandler.postDelayed(this, 60000) // tikrina kas 1 min
            }
        }
        activeHandler.post(activeRunnable)

        // =======LAIKINAS, mygtukas peržiūrėti Statistika.
        val btnStatistic = findViewById<Button>(R.id.btnOpenStatistic)
        btnStatistic.setOnClickListener {
            val intent = Intent(this, StatisticActivity::class.java)
            startActivity(intent)
        }


        // setting button
        findViewById<FloatingActionButton>(R.id.fabSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val nameTextView = findViewById<TextView>(R.id.home_page_vardas)
        val savedName = UserPreferences.getUserName(this)
        nameTextView.text = " $savedName"
    }

    override fun onResume() {
        super.onResume()
        val nameTextView = findViewById<TextView>(R.id.home_page_vardas)
        val savedName = UserPreferences.getUserName(this)
        nameTextView.text = " $savedName"

        // iš karto atnaujina žingsnius
        val goal = UserPreferences.getStepGoal(this)
        val steps = currentSteps
        val progressBar = findViewById<ProgressBar>(R.id.progressSteps)
        val label = findViewById<TextView>(R.id.tvStepsLabel)

        label.text = goal.toString()

        val newProgress = ((steps.toFloat() / goal) * 100).toInt()

        // Progress bar animacija
        val oldProgress = progressBar.progress
        val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", oldProgress, newProgress)
        animator.duration = 500 // ms
        animator.start()

        // Toast kai atnaujinamas tikslas
        if (goal != oldProgress && steps > 0) {
            Toast.makeText(this, "Tikslas atnaujintas: $goal žingsnių", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfileFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Sportininkas"
                    val weight = document.getDouble("weight") ?: 0.0
                    val height = document.getDouble("height") ?: 0.0

                    // suranda naudotojo varda is firebase
                    findViewById<TextView>(R.id.home_page_vardas).text = name

                    // sitas bus perkeltas veliau. -Julius
                    findViewById<TextView>(R.id.tvCalories)?.text = "${weight.toInt() * 13} "
                    findViewById<TextView>(R.id.tvDistance)?.text = String.format("%.1f", height * 0.01)

                    // offline naudojimui
                    UserPreferences.saveUserName(this, name)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Nepavyko gauti profilio duomenų", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            stepCounterService.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCounterService.stop()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (lastStepCount == -1) {
                lastStepCount = totalSteps
            }
            currentSteps = totalSteps - lastStepCount
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}


