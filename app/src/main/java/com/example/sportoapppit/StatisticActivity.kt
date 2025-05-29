package com.example.sportoapppit

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import android.content.res.ColorStateList

class StatisticActivity : BaseActivity() {

    private lateinit var barChart: BarChart

    private lateinit var btnDay: MaterialButton
    private lateinit var btnWeek: MaterialButton
    private lateinit var btnMonth: MaterialButton

    private lateinit var tvStep: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvActivity: TextView

    private lateinit var cardSteps: MaterialCardView
    private lateinit var cardDistance: MaterialCardView
    private lateinit var cardCalories: MaterialCardView
    private lateinit var cardActivity: MaterialCardView

    private lateinit var selectedMetric: String
    private lateinit var selectedPeriod: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistic_page)
        setupBottomNavigation()

        barChart = findViewById(R.id.statisticsChart)

        btnDay = findViewById(R.id.btn_day)
        btnWeek = findViewById(R.id.btn_week)
        btnMonth = findViewById(R.id.btn_month)

        tvStep = findViewById(R.id.tv_statistic_Step)
        tvDistance = findViewById(R.id.tv_statistic_Distance_text)
        tvCalories = findViewById(R.id.tv_statistic_Calories)
        tvActivity = findViewById(R.id.tv_statistic_Activity)

        cardSteps = findViewById(R.id.cv_steps)
        cardDistance = findViewById(R.id.cv_distance)
        cardCalories = findViewById(R.id.cv_kcal)
        cardActivity = findViewById(R.id.cv_activity)

        selectedMetric = "steps"
        selectedPeriod = "day"

        btnDay.setOnClickListener {
            selectedPeriod = "day"
            updateChart()
            updatePeriodSelection("day")
        }

        btnWeek.setOnClickListener {
            selectedPeriod = "week"
            updateChart()
            updatePeriodSelection("week")
        }

        btnMonth.setOnClickListener {
            selectedPeriod = "month"
            updateChart()
            updatePeriodSelection("month")
        }

        cardSteps.setOnClickListener {
            selectedMetric = "steps"
            updateChart()
            updateCardSelection("steps")
        }

        cardDistance.setOnClickListener {
            selectedMetric = "km"
            updateChart()
            updateCardSelection("km")
        }

        cardCalories.setOnClickListener {
            selectedMetric = "kcal"
            updateChart()
            updateCardSelection("kcal")
        }

        cardActivity.setOnClickListener {
            selectedMetric = "min"
            updateChart()
            updateCardSelection("min")
        }

        updateChart()
        updateCardSelection("steps")
        updatePeriodSelection("day")
    }

    private fun updateChart() {
        val values = generateDummyData(selectedMetric, selectedPeriod)
        val entries = values.mapIndexed { index, value -> BarEntry(index.toFloat() + 1, value) }

        val label = when (selectedMetric) {
            "steps" -> "Žingsniai"
            "km" -> "Kilometrai"
            "kcal" -> "Kalorijos"
            "min" -> "Minutės"
            else -> ""
        }

        val dataSet = BarDataSet(entries, label).apply {
            color = Color.parseColor("#00E1C0")
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        barChart.apply {
            data = BarData(dataSet)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            axisLeft.textColor = Color.WHITE
            xAxis.textColor = Color.WHITE
            legend.isEnabled = false
            description.isEnabled = false
            setFitBars(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateCardSelection(metric: String) {
        val primary = ContextCompat.getColor(this, R.color.primary)
        val normal = ContextCompat.getColor(this, R.color.navItemInactive)

        fun MaterialCardView.setStroke(active: Boolean) {
            this.strokeColor = if (active) primary else Color.TRANSPARENT
            this.strokeWidth = if (active) 6 else 0
        }

        cardSteps.setStroke(metric == "steps")
        cardDistance.setStroke(metric == "km")
        cardCalories.setStroke(metric == "kcal")
        cardActivity.setStroke(metric == "min")
    }

    private fun updatePeriodSelection(period: String) {
        val primary = ContextCompat.getColor(this, R.color.primary)
        val inactive = ContextCompat.getColor(this, R.color.navItemInactive)

        fun MaterialButton.setStyle(active: Boolean) {
            this.backgroundTintList = ColorStateList.valueOf(if (active) primary else inactive)
            this.setTextColor(Color.WHITE)
        }

        btnDay.setStyle(period == "day")
        btnWeek.setStyle(period == "week")
        btnMonth.setStyle(period == "month")
    }

    private fun generateDummyData(metric: String, period: String): List<Float> {
        return when (metric) {
            "steps" -> listOf(3000f, 4500f, 7000f, 8000f, 6200f, 9000f, 10000f)
            "km" -> listOf(2.1f, 3.4f, 5.0f, 4.8f, 3.7f, 6.2f, 7.0f)
            "kcal" -> listOf(180f, 260f, 300f, 320f, 270f, 330f, 350f)
            "min" -> listOf(20f, 35f, 40f, 45f, 30f, 50f, 55f)
            else -> emptyList()
        }
    }
}
