package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Clase que representa una receta con las carpetas a las que pertenece.
 */
data class RecetaConCarpetas(
    @Embedded val receta: Receta,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecetaCarpetaCrossRef::class,
            parentColumn = "recetaId",
            entityColumn = "carpetaId"
        )
    )
    val carpetas: List<Carpeta>
)
