package com.example.desafiodevlearning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class CursoAdapter(private val cursos: List<Curso>) :
    RecyclerView.Adapter<CursoAdapter.ViewHolder>() {

    private var onItemClick: OnItemClickListener? = null
    private var onEditClick: OnEditClickListener? = null
    private var onDeleteClick: OnDeleteClickListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutCursoInfo: LinearLayout = view.findViewById(R.id.layoutCursoInfo)
        val ivImagen: ImageView = view.findViewById(R.id.ivCursoImagen)
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.curso_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curso = cursos[position]
        holder.tvTitulo.text = curso.titulo
        holder.tvDescripcion.text = curso.descripcion
        holder.tvTipo.text = curso.tipo

        // Cargar imagen con Glide
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.dsmlogo) // Mientras carga
            .error(R.drawable.dsmlogo) // Si hay error
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache
            .centerCrop() // Ajustar imagen

        Glide.with(holder.itemView.context)
            .load(curso.imagen) // URL de la imagen
            .apply(requestOptions)
            .into(holder.ivImagen)

        // Click en el curso (para ver detalles en modal)
        holder.layoutCursoInfo.setOnClickListener {
            onItemClick?.onItemClick(curso)
        }

        // Click en botón Editar
        holder.btnEditar.setOnClickListener {
            onEditClick?.onEditClick(curso)
        }

        // Click en botón Eliminar
        holder.btnEliminar.setOnClickListener {
            onDeleteClick?.onDeleteClick(curso)
        }
    }

    override fun getItemCount(): Int {
        return cursos.size
    }

    // Listener para click en el curso (ver detalles)
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClick = listener
    }

    interface OnItemClickListener {
        fun onItemClick(curso: Curso)
    }

    // Listener para botón Editar
    fun setOnEditClickListener(listener: OnEditClickListener) {
        onEditClick = listener
    }

    interface OnEditClickListener {
        fun onEditClick(curso: Curso)
    }

    // Listener para botón Eliminar
    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        onDeleteClick = listener
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(curso: Curso)
    }
}