package com.example.recetarioboliviano.modelo.entidades

/**
 * Representa un paso de preparación de una receta.
 */
data class PasoPreparacion(
    val numero: Int,
    val descripcion: String,
    val imagenUri: String? = null
)