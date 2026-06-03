package com.example.recetarioboliviano.modelo.entidades

import java.io.Serializable

/**
 * Representa un paso de preparación de una receta.
 */
data class PasoPreparacion(
    val numero: Int,
    val descripcion: String,
    val imagenUri: String? = null
) : Serializable
