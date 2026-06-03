package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Receta que representa una receta en el recetario.
 * Incluye información sobre ingredientes, preparación, departamento y categoría.
 */
@Entity(tableName = "recetas")
data class Receta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val imagenUri: String? = null,
    val tiempoPreparacion: String,
    val cantidadPersonas: String,
    val ingredientes: String,
    val preparacion: String,
    val categoria: String, // postre, sopa, segundo
    val departamento: String,
    val esFavorito: Boolean = false,
    val esCreadaPorUsuario: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)
