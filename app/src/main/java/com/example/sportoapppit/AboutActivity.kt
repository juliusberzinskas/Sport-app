package com.example.sportoapppit

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Apie"

        val aboutText = """
            Sporto programėlė – PIT-22 projektas
            
            Sukūrė:
            • Odeta Juronienė
            • Rokas Ramanauskas
            • Julius Beržinskas

        """.trimIndent()

        findViewById<TextView>(R.id.tvAboutInfo).text = aboutText
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
