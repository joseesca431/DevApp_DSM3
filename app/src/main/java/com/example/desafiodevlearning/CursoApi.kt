package com.example.desafiodevlearning

import retrofit2.Call
import retrofit2.http.*

interface CursoApi {
    @GET("desafio/cursos")
    fun obtenerCursos(): Call<List<Curso>>

    @GET("desafio/cursos/{id}")
    fun obtenerCursoPorId(@Path("id") id: String): Call<Curso>

    @POST("desafio/cursos")
    fun crearCurso(@Body curso: Curso): Call<Curso>

    @PUT("desafio/cursos/{id}")
    fun actualizarCurso(@Path("id") id: String, @Body curso: Curso): Call<Curso>

    @DELETE("desafio/cursos/{id}")
    fun eliminarCurso(@Path("id") id: String): Call<Void>
}