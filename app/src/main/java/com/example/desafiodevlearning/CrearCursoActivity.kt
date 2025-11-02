package com.example.desafiodevlearning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CrearCursoActivity : AppCompatActivity() {

    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etTipo: EditText
    private lateinit var etEnlace: EditText
    private lateinit var etImagen: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_curso)

        // Inicializar vistas
        etTitulo = findViewById(R.id.editTextTitulo)
        etDescripcion = findViewById(R.id.editTextDescripcion)
        etTipo = findViewById(R.id.editTextTipo)
        etEnlace = findViewById(R.id.editTextEnlace)
        etImagen = findViewById(R.id.editTextImagen)
        btnGuardar = findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val tipo = etTipo.text.toString()
            val enlace = etEnlace.text.toString()
            val imagen = etImagen.text.toString()

            // Validaciones
            if (titulo.isBlank() || descripcion.isBlank() || tipo.isBlank()) {
                Toast.makeText(
                    this,
                    "Por favor completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Crear objeto Curso (id será asignado por la API)
            val curso = Curso(
                id = "0",
                titulo = titulo,
                descripcion = descripcion,
                tipo = tipo,
                enlace = enlace.ifBlank { "" },
                imagen = imagen.ifBlank { "" }
            )

            // Configurar Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("https://6906ffcbb1879c890ed8874f.mockapi.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(CursoApi::class.java)

            // Crear curso
            api.crearCurso(curso).enqueue(object : Callback<Curso> {
                override fun onResponse(call: Call<Curso>, response: Response<Curso>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@CrearCursoActivity,
                            "Curso creado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@CrearCursoActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("API", "Error al crear curso: $error")
                        Toast.makeText(
                            this@CrearCursoActivity,
                            "Error al crear el curso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Curso>, t: Throwable) {
                    Log.e("API", "Error al crear curso: ${t.message}")
                    Toast.makeText(
                        this@CrearCursoActivity,
                        "Error de conexión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}