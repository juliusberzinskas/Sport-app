package com.example.sportoapppit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_page)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordInput = findViewById<EditText>(R.id.editTextTextPassword)
        val repeatPasswordInput = findViewById<EditText>(R.id.editTextTextPassword2)
        val registerButton = findViewById<Button>(R.id.button)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()

            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Užpildykite visus laukus", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repeatPassword) {
                Toast.makeText(this, "Slaptažodžiai nesutampa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Slaptažodis turi būti bent 6 simboliai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase naudotojo registracija
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registracija sėkminga!", Toast.LENGTH_SHORT).show()

                        Log.d("DEBUG", "Saved birthdate: ${UserPreferences.getUserBirthdate(this)}")

                        // prisijungus nukelia i home activity
                        val intent = Intent(this, ProfileSignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Klaida: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // login redirection jei nepavyksta
        findViewById<TextView>(R.id.textView4).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // grizta atgal i starto activity
        findViewById<FloatingActionButton>(R.id.fabBack).setOnClickListener {
            finish()
        }
    }
}
