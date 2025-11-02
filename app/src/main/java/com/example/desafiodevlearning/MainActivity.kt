package com.example.desafiodevlearning

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CursoAdapter
    private lateinit var api: CursoApi
    private lateinit var btnLogout: Button
    private lateinit var tvUser: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var searchView: SearchView

    // Listas para manejar los datos
    private var cursosCompletos: List<Curso> = emptyList()
    private var cursosFiltrados: List<Curso> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        tvUser = findViewById(R.id.tvUser)
        btnLogout = findViewById(R.id.btnLogout)
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        val fabAgregar: FloatingActionButton = findViewById(R.id.fab_agregar)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Obtener usuario actual registrado en nuestro proyecto de firebase
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val userName = currentUser.displayName ?: currentUser.email
            tvUser.text = "Bienvenido, $userName"
        } else {
            // Si no hay usuario, redirigir a a la pantalla de Register
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://6906ffcbb1879c890ed8874f.mockapi.io/api/")//hecha en MockApi
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(CursoApi::class.java)

        // Cargar datos
        cargarDatos(api)

        // Configurar SearchView
        configurarBusqueda()

        // Botón de cerrar sesión
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón agregar curso
        fabAgregar.setOnClickListener {
            val intent = Intent(this, CrearCursoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos(api)
    }

    private fun configurarBusqueda() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarCursos(newText ?: "")
                return true
            }
        })
    }

    private fun filtrarCursos(texto: String) {
        if (texto.isEmpty()) {
            cursosFiltrados = cursosCompletos
        } else {
            cursosFiltrados = cursosCompletos.filter { curso ->
                curso.titulo.lowercase().contains(texto.lowercase())
            }
        }
        actualizarAdapter(cursosFiltrados)
    }

    private fun cargarDatos(api: CursoApi) {
        val call = api.obtenerCursos()
        call.enqueue(object : Callback<List<Curso>> {
            override fun onResponse(call: Call<List<Curso>>, response: Response<List<Curso>>) {
                if (response.isSuccessful) {
                    val cursos = response.body()
                    if (cursos != null) {
                        cursosCompletos = cursos
                        cursosFiltrados = cursos
                        actualizarAdapter(cursosFiltrados)
                    }
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("API", "Error al obtener cursos: $error")
                    Toast.makeText(
                        this@MainActivity,
                        "Error al obtener los cursos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Curso>>, t: Throwable) {
                Log.e("API", "Error al obtener cursos: ${t.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexión",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun actualizarAdapter(cursos: List<Curso>) {
        adapter = CursoAdapter(cursos)
        recyclerView.adapter = adapter

        // Click en el curso para ver detalles
        adapter.setOnItemClickListener(object : CursoAdapter.OnItemClickListener {
            override fun onItemClick(curso: Curso) {
                mostrarDialogDetalle(curso)
            }
        })

        // Click en botón Editar
        adapter.setOnEditClickListener(object : CursoAdapter.OnEditClickListener {
            override fun onEditClick(curso: Curso) {
                modificarCurso(curso)
            }
        })

        // Click en botón Eliminar
        adapter.setOnDeleteClickListener(object : CursoAdapter.OnDeleteClickListener {
            override fun onDeleteClick(curso: Curso) {
                eliminarCurso(curso, api)
            }
        })
    }

    private fun mostrarDialogDetalle(curso: Curso) {
        // Inflar el layout del diálogo
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_curso_detalle, null)

        // Referencias a las vistas del diálogo
        val ivDialogImagen = dialogView.findViewById<ImageView>(R.id.ivDialogImagen)
        val tvDialogTitulo = dialogView.findViewById<TextView>(R.id.tvDialogTitulo)
        val tvDialogTipo = dialogView.findViewById<TextView>(R.id.tvDialogTipo)
        val tvDialogDescripcion = dialogView.findViewById<TextView>(R.id.tvDialogDescripcion)
        val tvDialogEnlace = dialogView.findViewById<TextView>(R.id.tvDialogEnlace)
        val btnVerEnNavegador = dialogView.findViewById<Button>(R.id.btnVerEnNavegador)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrar)

        // Llenar datos
        tvDialogTitulo.text = curso.titulo
        tvDialogTipo.text = "Tipo: ${curso.tipo}"
        tvDialogDescripcion.text = curso.descripcion
        tvDialogEnlace.text = if (curso.enlace.isNotEmpty()) curso.enlace else "No disponible"

        // Cargar imagen con Glide
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.dsmlogo)
            .error(R.drawable.dsmlogo)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()

        Glide.with(this)
            .load(curso.imagen)
            .apply(requestOptions)
            .into(ivDialogImagen)

        // Crear el diálogo
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Botón Ver en Navegador
        btnVerEnNavegador.setOnClickListener {
            if (curso.enlace.isNotEmpty() && curso.enlace.startsWith("http")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(curso.enlace))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enlace no disponible", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cerrar
        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun modificarCurso(curso: Curso) {
        val intent = Intent(this, ActualizarCursoActivity::class.java)
        intent.putExtra("curso_id", curso.id)
        intent.putExtra("titulo", curso.titulo)
        intent.putExtra("descripcion", curso.descripcion)
        intent.putExtra("tipo", curso.tipo)
        intent.putExtra("enlace", curso.enlace)
        intent.putExtra("imagen", curso.imagen)
        startActivity(intent)
    }

    private fun eliminarCurso(curso: Curso, api: CursoApi) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de eliminar '${curso.titulo}'?")
            .setPositiveButton("Sí") { _, _ ->
                val llamada = api.eliminarCurso(curso.id)
                llamada.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@MainActivity,
                                "Curso eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                            cargarDatos(api)
                        } else {
                            val error = response.errorBody()?.string()
                            Log.e("API", "Error al eliminar curso: $error")
                            Toast.makeText(
                                this@MainActivity,
                                "Error al eliminar curso",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("API", "Error al eliminar curso: $t")
                        Toast.makeText(
                            this@MainActivity,
                            "Error de conexión",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .setNegativeButton("No", null)
            .show()
    }
}