package com.example.sportoapppit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        val today = LocalDate.now()

        val days = mutableListOf<CalendarDay>()
        for (i in -1..5) {
            val date = today.plusDays(i.toLong())
            days.add(
                CalendarDay(
                    date = date,
                    isToday = date == today,
                    hasWorkout = (date.dayOfMonth == 5) // pvz. treniruotė tik 5 d.
                )
            )
        }

        val calendarRecycler = findViewById<RecyclerView>(R.id.calendarRecycler)
        val adapter = CalendarDayAdapter(days) { selectedDate ->
            println("Pasirinkta data: $selectedDate")
        }

        calendarRecycler.layoutManager = GridLayoutManager(this, 7, RecyclerView.VERTICAL, false)
        calendarRecycler.adapter = adapter

        // --- ŠIANDIENOS TIKSLAI BLOKAS ---
        val tvGoalTitle = findViewById<TextView>(R.id.tvGoalTitle)
        val tvGoalDescription = findViewById<TextView>(R.id.tvGoalDescription)
        val btnStartGoal = findViewById<Button>(R.id.home_page_btnStartGoal)
        val goalImage = findViewById<ImageView>(R.id.ivGoalImage)

        // Simuliuojam kad šiandien bėgimas
        val plannedToday = "bėgimas" // arba null jei nieko


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
            // Jei nėra treniruotės – paslepiam visą bloką (gal LinearLayout?)
            findViewById<View>(R.id.goalContainer).visibility = View.GONE
        }

    }
}
