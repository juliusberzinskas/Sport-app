package com.example.sportoapppit

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryActivity : BaseActivity() {

    private lateinit var historyRecycler: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_history)

        historyRecycler = findViewById(R.id.recyclerHistory)
        emptyView = findViewById(R.id.tvEmpty)

        val sessions = loadWorkoutHistory()

        if (sessions.isEmpty()) {
            emptyView.text = "Nėra treniruočių istorijos."
            emptyView.visibility = android.view.View.VISIBLE
        } else {
            emptyView.visibility = android.view.View.GONE
            val grouped = sessions.groupBy { formatDateGroup(it.dateTime) }
            historyRecycler.layoutManager = LinearLayoutManager(this)
            historyRecycler.adapter = WorkoutHistoryAdapter(grouped)
        }
    }

    private fun loadWorkoutHistory(): List<WorkoutSession> {
        val prefs = getSharedPreferences("workout_data", Context.MODE_PRIVATE)
        val json = prefs.getString("workout_history", null) ?: return emptyList()
        val type = object : TypeToken<List<WorkoutSession>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun formatDateGroup(dateStr: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = format.parse(dateStr) ?: return "Kita"

        val today = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }

        return when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Šiandien"
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "Vakar"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }
    }
}
