package com.example.sportoapppit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private val IMAGE_PICK_CODE = 1001
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Paskyros Nustatymai"

        imgProfile = findViewById(R.id.imgProfile)

        val etName = findViewById<EditText>(R.id.etName)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etCurrent = findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = findViewById<EditText>(R.id.etNewPassword)
        val etRepeat = findViewById<EditText>(R.id.etRepeatPassword)
        val btnConfirmChanges = findViewById<Button>(R.id.btnConfirmChanges)

        // Pre-fill fields
        etName.setText(UserPreferences.getUserName(this))
        etWeight.setText(UserPreferences.getUserWeight(this).toInt().toString())
        etHeight.setText(UserPreferences.getUserHeight(this).toInt().toString())

        findViewById<FloatingActionButton>(R.id.fabPickImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnConfirmChanges.setOnClickListener {
            val name = etName.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()
            val heightStr = etHeight.text.toString().trim()
            val weight = weightStr.toDoubleOrNull()
            val height = heightStr.toDoubleOrNull()

            val current = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val repeat = etRepeat.text.toString()

            val user = auth.currentUser
            val email = user?.email
            val userId = user?.uid ?: return@setOnClickListener

            if (name.isEmpty() || weight == null || height == null || weight <= 0 || height <= 0) {
                Toast.makeText(this, "Užpildykite visus laukus teisingai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save locally
            UserPreferences.saveUserName(this, name)
            UserPreferences.saveUserWeight(this, weight)
            UserPreferences.saveUserHeight(this, height)

            // Save to Firestore
            val updates = mapOf(
                "name" to name,
                "weight" to weight,
                "height" to height
            )

            db.collection("users").document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Duomenys atnaujinti", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Klaida išsaugant", Toast.LENGTH_SHORT).show()
                }

            // Add stat entry to history
            val age = UserPreferences.getUserAge(this)
            val gender = UserPreferences.getUserGender(this) // Add getUserGender if missing

            val bmi = weight / ((height / 100) * (height / 100))
            val fatPercent = estimateFatPercentage(weight, height, age, gender)

            val stat = ProfileStats.ProfileStat(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                weight = weight,
                height = height,
                bmi = bmi,
                fatPercent = fatPercent
            )

            db.collection("users").document(userId).collection("stats").add(stat)

            // Password change (optional)
            if (current.isNotEmpty() || newPass.isNotEmpty() || repeat.isNotEmpty()) {
                if (email == null) {
                    Toast.makeText(this, "Trūksta el. pašto", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (newPass.length < 6) {
                    Toast.makeText(this, "Slaptažodis per trumpas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (newPass != repeat) {
                    Toast.makeText(this, "Slaptažodžiai nesutampa", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val credential = EmailAuthProvider.getCredential(email, current)
                user.reauthenticate(credential).addOnSuccessListener {
                    user.updatePassword(newPass).addOnSuccessListener {
                        Toast.makeText(this, "Slaptažodis atnaujintas", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Nepavyko atnaujinti slaptažodžio", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Neteisingas dabartinis slaptažodis", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                try {
                    val rotatedBitmap = decodeBitmapWithCorrectOrientation(imageUri)
                    if (rotatedBitmap != null) {
                        imgProfile.setImageBitmap(rotatedBitmap)

                        contentResolver.takePersistableUriPermission(
                            imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        UserPreferences.saveAvatarUri(this, imageUri.toString())
                    } else {
                        Toast.makeText(this, "Nepavyko apdoroti paveikslėlio", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Klaida įkeliant paveikslėlį", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun decodeBitmapWithCorrectOrientation(uri: Uri): Bitmap? {
        val inputStream1 = contentResolver.openInputStream(uri) ?: return null
        val exif = ExifInterface(inputStream1)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStream1.close()

        val inputStream2 = contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream2) ?: return null
        inputStream2.close()

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Example body fat formula (same as before)
    private fun estimateFatPercentage(weight: Double, height: Double, age: Int, gender: String): Double {
        val bmi = weight / ((height / 100) * (height / 100))
        return if (gender == "male") {
            (1.20 * bmi) + (0.23 * age) - 16.2
        } else {
            (1.20 * bmi) + (0.23 * age) - 5.4
        }
    }
}
