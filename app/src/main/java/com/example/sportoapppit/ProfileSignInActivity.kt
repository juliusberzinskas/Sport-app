package com.example.sportoapppit

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileSignInActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_CODE = 1001
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_sign_in_page)

        imgProfile = findViewById(R.id.imgProfile)
        val fabPickImage = findViewById<FloatingActionButton>(R.id.fabPickImage)
        val etName = findViewById<EditText>(R.id.etName)
        val etBirthday = findViewById<EditText>(R.id.etBirthday)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val genderGroup = findViewById<RadioGroup>(R.id.genderGroup)

        // --- avataro pasirinkimas ---
        fabPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // --- Gimimo datos pasirinkimas ---
        etBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(this, { _, y, m, d ->
                val formatted = String.format("%04d-%02d-%02d", y, m + 1, d)
                etBirthday.setText(formatted)
            }, year, month, day)
            dialog.show()
        }

        // --- profilio išsaugojimas ---
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val birthday = etBirthday.text.toString().trim()
            val weight = etWeight.text.toString().toDoubleOrNull()
            val height = etHeight.text.toString().toDoubleOrNull()
            val avatar = selectedImageUri?.toString()

            if (name.isEmpty() || birthday.isEmpty() || weight == null || height == null) {
                Toast.makeText(this, "Užpildykite visus laukus", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Naudotojas neprisijungęs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGender = when (genderGroup.checkedRadioButtonId) {
                R.id.radioMale -> "male"
                R.id.radioFemale -> "female"
                else -> null
            }

            val userMap = hashMapOf(
                "name" to name,
                "birthday" to birthday,
                "weight" to weight,
                "height" to height,
                "gender" to selectedGender,
                "avatarUri" to avatar
            )

            if (selectedGender == null) {
                Toast.makeText(this, "Pasirinkite lytį", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(userId).set(userMap)
                .addOnSuccessListener {
                    // ✅ Save locally for use in Settings/Home/etc.
                    UserPreferences.saveUserName(this, name)
                    UserPreferences.saveUserWeight(this, weight)
                    UserPreferences.saveUserHeight(this, height)
                    if (avatar != null) {
                        UserPreferences.saveAvatarUri(this, avatar)
                    }

                    Toast.makeText(this, "Profilis išsaugotas!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Nepavyko išsaugoti profilio", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val stream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                imgProfile.setImageBitmap(bitmap)
            }
        }
    }
}
