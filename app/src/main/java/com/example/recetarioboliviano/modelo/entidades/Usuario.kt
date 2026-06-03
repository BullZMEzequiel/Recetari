package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Usuario que representa la información del usuario registrado en la aplicación.
 * Una vez registrado, el nombre y departamento no son editables.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val departamento: String,
    val pais: String = "Bolivia",
    val avatarUri: String? = null,
    val fechaRegistro: Long = System.currentTimeMillis()
)
