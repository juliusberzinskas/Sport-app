package com.example.sportoapppit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnSendReset: Button
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_pass_page)

        etEmail = findViewById(R.id.etEmailReset)
        btnSendReset = findViewById(R.id.btnSendReset)

        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Įveskite el. pašto adresą", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "El. laiškas išsiųstas: patikrinkite savo paštą", Toast.LENGTH_LONG).show()
                    finish() // go back to login screen
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Nepavyko išsiųsti: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
