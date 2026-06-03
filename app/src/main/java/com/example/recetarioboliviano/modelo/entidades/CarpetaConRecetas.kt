package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Clase que representa una carpeta con su lista de recetas asociadas.
 */
data class CarpetaConRecetas(
    @Embedded val carpeta: Carpeta,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecetaCarpetaCrossRef::class,
            parentColumn = "carpetaId",
            entityColumn = "recetaId"
        )
    )
    val recetas: List<Receta>
)
