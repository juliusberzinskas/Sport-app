package com.example.sportoapppit

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val name = UserPreferences.getUserName(this)
        findViewById<TextView>(R.id.tvSettingsName).text = name

        val weight = UserPreferences.getUserWeight(this)
        val height = UserPreferences.getUserHeight(this)

        findViewById<TextView>(R.id.tvSettingsWeight).text = "Svoris: ${weight.toInt()} kg"
        findViewById<TextView>(R.id.tvSettingsHeight).text = "Ūgis: ${height.toInt()} cm"

        val avatarUri = UserPreferences.getAvatarUri(this)
        val imageView = findViewById<ImageView>(R.id.imgSettingsAvatar)

        if (avatarUri != null) {
            try {
                val uri = avatarUri.toUri()
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.icon_file_upload)
                }
            } catch (e: SecurityException) {
                imageView.setImageResource(R.drawable.icon_file_upload)
                e.printStackTrace()
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.icon_file_upload)
                e.printStackTrace()
            }
        } else {
            imageView.setImageResource(R.drawable.icon_file_upload)
        }

        // Logout
        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nustatymai"

        findViewById<LinearLayout>(R.id.rowAccount).setOnClickListener {
            startActivity(Intent(this, AccountSettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.rowApp).setOnClickListener {
            startActivity(Intent(this, AppSettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.rowAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
