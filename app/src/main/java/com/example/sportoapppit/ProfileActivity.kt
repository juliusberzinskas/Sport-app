package com.example.sportoapppit

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class ProfileActivity : BaseActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var imgAvatar: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()

        // setting button
        findViewById<FloatingActionButton>(R.id.fabSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        tvName = findViewById(R.id.tvName)
        tvAge = findViewById(R.id.tvAge)
        imgAvatar = findViewById(R.id.tvAvatar)

        loadProfileData()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerProfileStats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("stats")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val statList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProfileStats.ProfileStat::class.java)
                }
                recyclerView.adapter = ProfileStats.ProfileStatAdapter(statList)

                setupWeightChart(statList)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Nepavyko įkelti statistikos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileData() {
        val name = UserPreferences.getUserName(this)
        val birthdate = UserPreferences.getUserBirthdate(this)
        val avatarUri = UserPreferences.getAvatarUri(this)
        val gender = UserPreferences.getUserGender(this)

        fun calculateAge(birthdate: String): Int {
            return try {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dob = formatter.parse(birthdate)
                val today = Calendar.getInstance()
                val birth = Calendar.getInstance().apply { time = dob!! }

                var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            } catch (e: Exception) {
                0
            }
        }

        tvName.text = name
        val age = birthdate?.let { calculateAge(it) } ?: 0
        tvAge.text = "$age m."

        avatarUri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it.toUri())
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imgAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgAvatar.setImageResource(R.drawable.icon_file_upload)
            }
        }

        val tvWeight = findViewById<TextView>(R.id.tvWeight)
        val tvHeight = findViewById<TextView>(R.id.tvHeight)

        // Load user data
        val weight = UserPreferences.getUserWeight(this)
        val height = UserPreferences.getUserHeight(this)

        tvWeight.text = "${weight.toInt()} kg"
        tvHeight.text = "${height.toInt()} cm"

        val genderIcon = findViewById<ImageView>(R.id.imgGenderIcon)
        when (gender.lowercase()) {
            "male" -> {
                genderIcon.setImageResource(R.drawable.ic_gender_male)
                genderIcon.visibility = View.VISIBLE
            }
            "female" -> {
                genderIcon.setImageResource(R.drawable.ic_gender_female)
                genderIcon.visibility = View.VISIBLE
            }
            else -> genderIcon.visibility = View.GONE
        }
    }

    private fun setupWeightChart(stats: List<ProfileStats.ProfileStat>) {
        val chart = findViewById<LineChart>(R.id.chartWeightHistory)

        val entries = stats.mapIndexed { index, stat ->
            Entry(index.toFloat(), stat.weight.toFloat())
        }

        val dataSet = LineDataSet(entries, "Svoris (kg)").apply {
            color = Color.CYAN
            setCircleColor(Color.CYAN)
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            lineWidth = 2f
        }

        val lineData = LineData(dataSet)
        chart.data = lineData

        chart.description.text = "Svorio istorija"
        chart.axisLeft.textColor = Color.WHITE
        chart.axisRight.isEnabled = false
        chart.xAxis.textColor = Color.WHITE
        chart.legend.textColor = Color.WHITE
        chart.invalidate() // Refresh chart
    }

}
