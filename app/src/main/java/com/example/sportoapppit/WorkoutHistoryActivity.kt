package com.example.sportoapppit

import WorkoutSession
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutHistoryActivity : BaseActivity() {

    private lateinit var historyRecycler: RecyclerView
    private lateinit var emptyView: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_history)
        setupBottomNavigation()

        // setting button
        findViewById<FloatingActionButton>(R.id.fabSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        historyRecycler = findViewById(R.id.recyclerHistory)
        emptyView = findViewById(R.id.tvEmpty)

        loadWorkoutHistoryFromFirestore()
    }

    private fun loadWorkoutHistoryFromFirestore() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .collection("workouts")
            .get()
            .addOnSuccessListener { result ->
                val sessions = result.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }

                if (sessions.isEmpty()) {
                    emptyView.text = "Nėra treniruočių istorijos."
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.GONE

                    val sortedSessions = sessions.sortedByDescending { it.dateTime }
                    val grouped = sortedSessions.groupBy { formatDateGroup(it.dateTime) }

                    historyRecycler.layoutManager = LinearLayoutManager(this)
                    historyRecycler.adapter = WorkoutHistoryAdapter(grouped)
                }
            }
            .addOnFailureListener {
                emptyView.text = "Nepavyko gauti duomenų."
                emptyView.visibility = View.VISIBLE
            }
    }

    private fun formatDateGroup(dateStr: String): String {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val date = format.parse(dateStr) ?: return "Kita"

            val today = java.util.Calendar.getInstance()
            val cal = java.util.Calendar.getInstance().apply { time = date }

            when {
                cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> "Šiandien"
                cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) - 1 -> "Vakar"
                else -> java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            "Kita"
        }
    }
}
