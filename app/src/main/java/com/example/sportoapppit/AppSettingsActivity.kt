package com.example.sportoapppit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class AppSettingsActivity : AppCompatActivity() {

    private val stepGoalOptions = arrayOf("10000", "18000", "25000", "35000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        val spinner = findViewById<Spinner>(R.id.spinnerStepGoals)
        val etCustom = findViewById<EditText>(R.id.etCustomGoal)
        val btnSave = findViewById<Button>(R.id.btnSaveGoal)

        // Spineris
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stepGoalOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val savedGoal = UserPreferences.getStepGoal(this).toString()
        val position = stepGoalOptions.indexOf(savedGoal)
        if (position >= 0) spinner.setSelection(position)

        // Save migtuko paspaudimo logika
        btnSave.setOnClickListener {
            val selected = spinner.selectedItem.toString()
            val custom = etCustom.text.toString()

            val finalGoal = when {
                custom.isNotEmpty() -> custom.toIntOrNull()
                else -> selected.toIntOrNull()
            }

            if (finalGoal != null && finalGoal >= 1000) {
                UserPreferences.saveStepGoal(this, finalGoal)
                Toast.makeText(this, "Tikslas išsaugotas: $finalGoal žingsnių", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Neteisingas tikslas", Toast.LENGTH_SHORT).show()
            }
        }

        val switchReminder = findViewById<Switch>(R.id.switchReminder)
        switchReminder.isChecked = UserPreferences.isReminderEnabled(this)

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setReminderEnabled(this, isChecked)
            if (isChecked) {
                scheduleDailyReminder()
                Toast.makeText(this, "Priminimas įjungtas", Toast.LENGTH_SHORT).show()
            } else {
                cancelDailyReminder()
                Toast.makeText(this, "Priminimas išjungtas", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun scheduleDailyReminder() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // next 24 hours
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelDailyReminder() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

}