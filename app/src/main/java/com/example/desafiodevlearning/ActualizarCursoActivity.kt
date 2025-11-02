package com.example.desafiodevlearning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ActualizarCursoActivity : AppCompatActivity() {

    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etTipo: EditText
    private lateinit var etEnlace: EditText
    private lateinit var etImagen: EditText
    private lateinit var btnActualizar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_curso)

        // Inicializar vistas
        etTitulo = findViewById(R.id.tituloEditText)
        etDescripcion = findViewById(R.id.descripcionEditText)
        etTipo = findViewById(R.id.tipoEditText)
        etEnlace = findViewById(R.id.enlaceEditText)
        etImagen = findViewById(R.id.imagenEditText)
        btnActualizar = findViewById(R.id.actualizarButton)

        // Obtener datos del intent
        val cursoId = intent.getStringExtra("curso_id") ?: ""
        val titulo = intent.getStringExtra("titulo") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val tipo = intent.getStringExtra("tipo") ?: ""
        val enlace = intent.getStringExtra("enlace") ?: ""
        val imagen = intent.getStringExtra("imagen") ?: ""

        // Llenar los campos con los datos actuales
        etTitulo.setText(titulo)
        etDescripcion.setText(descripcion)
        etTipo.setText(tipo)
        etEnlace.setText(enlace)
        etImagen.setText(imagen)

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://6906ffcbb1879c890ed8874f.mockapi.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CursoApi::class.java)

        // Configurar botón de actualización
        btnActualizar.setOnClickListener {
            val tituloActualizado = etTitulo.text.toString()
            val descripcionActualizada = etDescripcion.text.toString()
            val tipoActualizado = etTipo.text.toString()
            val enlaceActualizado = etEnlace.text.toString()
            val imagenActualizada = etImagen.text.toString()

            // Validaciones
            if (tituloActualizado.isBlank() || descripcionActualizada.isBlank() || tipoActualizado.isBlank()) {
                Toast.makeText(
                    this,
                    "Por favor completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Crear objeto Curso actualizado
            val cursoActualizado = Curso(
                id = cursoId,
                titulo = tituloActualizado,
                descripcion = descripcionActualizada,
                tipo = tipoActualizado,
                enlace = enlaceActualizado,
                imagen = imagenActualizada
            )

            val jsonCursoActualizado = Gson().toJson(cursoActualizado)
            Log.d("API", "JSON enviado: $jsonCursoActualizado")

            // Realizar solicitud PUT
            api.actualizarCurso(cursoId, cursoActualizado).enqueue(object : Callback<Curso> {
                override fun onResponse(call: Call<Curso>, response: Response<Curso>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(
                            this@ActualizarCursoActivity,
                            "Curso actualizado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@ActualizarCursoActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("API", "Error al actualizar curso: $error")
                        Toast.makeText(
                            this@ActualizarCursoActivity,
                            "Error al actualizar el curso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Curso>, t: Throwable) {
                    Log.e("API", "onFailure: ${t.message}")
                    Toast.makeText(
                        this@ActualizarCursoActivity,
                        "Error de conexión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}