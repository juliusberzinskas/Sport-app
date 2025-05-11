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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.exifinterface.media.ExifInterface

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private val IMAGE_PICK_CODE = 1001
    private val dummyPassword = "password123" // sujungti veliau su backendu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Paskyros Nustatymai"

        imgProfile = findViewById(R.id.imgProfile)

        // nuotraukos pasirinkimas
        val fabPickImage = findViewById<FloatingActionButton>(R.id.fabPickImage)
        fabPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Vardo keitimas
        val etName = findViewById<EditText>(R.id.etName)
        val btnConfirmName = findViewById<Button>(R.id.btnConfirmName)
        btnConfirmName.setOnClickListener {
            val newName = etName.text.toString()
            if (newName.isNotEmpty()) {
                UserPreferences.saveUserName(this, newName)
                Toast.makeText(this, "Vardas atnaujintas!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Įveskite vardą", Toast.LENGTH_SHORT).show()
            }
        }

        // Slaptažodžio keitimas
        val etCurrent = findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = findViewById<EditText>(R.id.etNewPassword)
        val etRepeat = findViewById<EditText>(R.id.etRepeatPassword)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener {
            val current = etCurrent.text.toString()
            val new = etNew.text.toString()
            val repeat = etRepeat.text.toString()

            when {
                current != dummyPassword -> Toast.makeText(this, "Neteisingas dabartinis slaptažodis", Toast.LENGTH_SHORT).show()
                new.length < 6 -> Toast.makeText(this, "Slaptažodis per trumpas", Toast.LENGTH_SHORT).show()
                new != repeat -> Toast.makeText(this, "Slaptažodžiai nesutampa", Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(this, "Slaptažodis pakeistas!", Toast.LENGTH_SHORT).show()
                    // siusti i backend
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

                        // išsaugo URI
                        contentResolver.takePersistableUriPermission(
                            imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        UserPreferences.saveAvatarUri(this, imageUri.toString())
                    } else {
                        Toast.makeText(this, "Nepavyko apdoroti paveikslėlio", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Trūksta leidimo prie paveikslėlio", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                } catch (e: Exception) {
                    Toast.makeText(this, "Įvyko klaida įkeliant paveikslėlį", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun decodeBitmapWithCorrectOrientation(uri: Uri): Bitmap? {
        // 1. pakrauna nuotrauka su Exif + bitmap
        val inputStream1 = contentResolver.openInputStream(uri) ?: return null
        val exif = ExifInterface(inputStream1)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStream1.close()

        // 2. atnaujina ikelta INPUT su decodeStream
        val inputStream2 = contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream2) ?: return null
        inputStream2.close()

        // 3. Pasuka nuotrauka pagal Exif orientacija nes BitmapFactory nesugeba
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
}
