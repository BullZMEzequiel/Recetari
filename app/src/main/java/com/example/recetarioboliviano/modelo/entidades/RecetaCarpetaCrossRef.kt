package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Tabla de relación muchos a muchos entre Recetas y Carpetas.
 */
@Entity(
    tableName = "receta_carpeta_cross_ref",
    primaryKeys = ["recetaId", "carpetaId"],
    foreignKeys = [
        ForeignKey(
            entity = Receta::class,
            parentColumns = ["id"],
            childColumns = ["recetaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Carpeta::class,
            parentColumns = ["id"],
            childColumns = ["carpetaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carpetaId"), Index("recetaId")]
)
data class RecetaCarpetaCrossRef(
    val recetaId: Int,
    val carpetaId: Int
)
