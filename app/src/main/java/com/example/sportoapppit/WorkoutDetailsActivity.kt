package com.example.sportoapppit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WorkoutDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_details)

        val session = intent.getSerializableExtra("workout") as? WorkoutSession ?: return

        val tvType = findViewById<TextView>(R.id.tvWorkoutType)
        val tvDate = findViewById<TextView>(R.id.tv_activity_workout_date)
        val tvTimer = findViewById<TextView>(R.id.tvTimerDetail)
        val tvCalories = findViewById<TextView>(R.id.tvWorkout_detail_Calories)
        val tvDistance = findViewById<TextView>(R.id.tv_workout_detail_Distance)
        val tvSteps = findViewById<TextView>(R.id.workout_Steps)
        val tvSpeed = findViewById<TextView>(R.id.tv_workout_detail_Speed)

        val tvVidKcal = findViewById<TextView>(R.id.tv_workout_detail_vidKcal)
        val tvVidSpeed = findViewById<TextView>(R.id.tv_workout_detail_VidSpeed)
        val tvVidSteps = findViewById<TextView>(R.id.tv_workout_detail_VidSteps)
        val tvStepLength = findViewById<TextView>(R.id.tv_workout_detail_StepLength)

        val imgMap = findViewById<ImageView>(R.id.imgMapPreview)

        tvType.text = session.type.replaceFirstChar { it.uppercase() }
        tvDate.text = formatDate(session.dateTime)
        tvTimer.text = formatDuration(session.durationSec)
        tvCalories.text = "${session.calories}"
        tvDistance.text = String.format("%.2f", session.distanceKm)
        tvSteps.text = "${session.steps}"
        tvSpeed.text = calculateSpeed(session)

        // Realistiški apsaugoti skaičiavimai
        val vidKcal = if (session.distanceKm >= 0.1) session.calories / session.distanceKm else null
        val vidSpeed = if (session.distanceKm >= 0.1 && session.durationSec >= 30) session.durationSec / 60.0 / session.distanceKm else null
        val cadence = if (session.steps > 0 && session.durationSec >= 30) (session.steps * 60.0) / session.durationSec else null
        val stepLength = if (session.steps > 0 && session.distanceKm > 0.01) (session.distanceKm * 1000) / session.steps else null

        tvVidKcal.text = vidKcal?.let { String.format("%.1f", it) } ?: "--"
        tvVidSpeed.text = vidSpeed?.let { formatMinutesPerKm(it) } ?: "--"
        tvVidSteps.text = cadence?.let { String.format("%.0f", it) } ?: "--"
        tvStepLength.text = stepLength?.let { String.format("%.0f", it * 100) } ?: "--"

        if (!session.mapImagePath.isNullOrEmpty()) {
            val file = File(session.mapImagePath)
            if (file.exists()) {
                imgMap.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                imgMap.visibility = ImageView.VISIBLE
            }
        }

        findViewById<ImageView>(R.id.fabBack).setOnClickListener {
            finish()
        }
    }

    private fun formatDate(raw: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse(raw)
            val output = SimpleDateFormat("MMMM d, yyyy", Locale("lt"))
            output.format(date!!)
        } catch (e: Exception) {
            raw
        }
    }

    private fun formatDuration(sec: Int): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun formatMinutesPerKm(minPerKm: Double): String {
        val min = minPerKm.toInt()
        val sec = ((minPerKm - min) * 60).toInt()
        return String.format("%d:%02d", min, sec)
    }

    private fun calculateSpeed(session: WorkoutSession): String {
        val hours = session.durationSec / 3600.0
        return if (hours > 0) String.format("%.1f", session.distanceKm / hours) else "--"
    }
}
