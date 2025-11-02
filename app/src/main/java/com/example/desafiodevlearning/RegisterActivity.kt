package com.example.desafiodevlearning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNombreCompleto: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvRedirectLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar vistas
        etNombreCompleto = findViewById(R.id.etSNombreCompleto)
        etEmail = findViewById(R.id.etSEmailAddress)
        etPass = findViewById(R.id.etSPassword)
        btnSignUp = findViewById(R.id.btnSSigned)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        // Botón de registro
        btnSignUp.setOnClickListener {
            signUpUser()
        }

        // Redirección a Login
        tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar si el usuario ya tiene sesión activa
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuario ya tiene sesión → enviarlo al MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUpUser() {
        val nombre = etNombreCompleto.text.toString()
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()

        // Validaciones
        if (nombre.isBlank()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Email y contraseña no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear usuario en Firebase
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al registrar: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}