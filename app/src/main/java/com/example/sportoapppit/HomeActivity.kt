package com.example.sportoapppit

import android.os.Bundle
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

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

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

            btnStartGoal.setOnClickListener {
                println("Pradedama treniruotė: $plannedToday")
            }

        } else {
            findViewById<View>(R.id.goalContainer).visibility = View.GONE
        }

        // --- 4. Aktyvumo duomenys ---
        val steps = 11857
        val stepsGoal = 18000
        val calories = 850
        val distance = 5.2
        val time = 120

        findViewById<TextView>(R.id.tvStepsCount).text = steps.toString()
        findViewById<TextView>(R.id.tvStepsLabel).text = stepsGoal.toString()
        findViewById<ProgressBar>(R.id.progressSteps).progress = ((steps.toDouble() / stepsGoal) * 100).toInt()

        findViewById<TextView>(R.id.tvCalories).text = calories.toString()
        findViewById<TextView>(R.id.tvDistance).text = distance.toString()
        findViewById<TextView>(R.id.tvTime).text = time.toString()
    }
}
