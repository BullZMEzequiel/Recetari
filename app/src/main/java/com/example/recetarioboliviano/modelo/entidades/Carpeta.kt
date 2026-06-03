package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una carpeta o colección de recetas creada por el usuario.
 */
@Entity(tableName = "carpetas")
data class Carpeta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)
