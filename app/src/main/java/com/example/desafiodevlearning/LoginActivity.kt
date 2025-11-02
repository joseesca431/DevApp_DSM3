package com.example.desafiodevlearning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRedirectSignUp: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etEmail = findViewById(R.id.editTextTextEmailAddress)
        etPass = findViewById(R.id.editTextTextPassword)
        btnLogin = findViewById(R.id.button_login)
        tvRedirectSignUp = findViewById(R.id.tvRedirectSignUp)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Botón de login
        btnLogin.setOnClickListener {
            login()
        }

        // Redirección a Register
        tvRedirectSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()

        // Validaciones
        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Email y contraseña no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        // Iniciar sesión con Firebase
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al iniciar sesión: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}