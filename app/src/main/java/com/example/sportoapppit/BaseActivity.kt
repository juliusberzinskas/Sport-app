package com.example.sportoapppit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            ?: return

        when (this) {
            is HomeActivity -> bottomNavigationView.selectedItemId = R.id.navigation_home
            is WorkoutHistoryActivity -> bottomNavigationView.selectedItemId = R.id.navigation_workout
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (this !is HomeActivity) {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivity(intent)
                    }
                    true
                }
                R.id.navigation_workout -> {
                    if (this !is WorkoutHistoryActivity) {
                        val intent = Intent(this, WorkoutHistoryActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }

}