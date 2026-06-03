package com.example.recetarioboliviano.modelo.dao

import androidx.room.*
import com.example.recetarioboliviano.modelo.entidades.Carpeta
import com.example.recetarioboliviano.modelo.entidades.CarpetaConRecetas
import com.example.recetarioboliviano.modelo.entidades.RecetaCarpetaCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface CarpetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCarpeta(carpeta: Carpeta): Long

    @Update
    suspend fun actualizarCarpeta(carpeta: Carpeta)

    @Delete
    suspend fun eliminarCarpeta(carpeta: Carpeta)

    @Query("SELECT * FROM carpetas ORDER BY nombre ASC")
    fun obtenerTodasLasCarpetas(): Flow<List<Carpeta>>

    @Query("SELECT * FROM carpetas WHERE nombre LIKE '%' || :busqueda || '%' ORDER BY nombre ASC")
    fun buscarCarpetas(busqueda: String): Flow<List<Carpeta>>

    @Query("SELECT * FROM recetas WHERE id IN (SELECT recetaId FROM receta_carpeta_cross_ref WHERE carpetaId = :carpetaId) AND (titulo LIKE '%' || :busqueda || '%' OR ingredientes LIKE '%' || :busqueda || '%')")
    fun buscarRecetasDeCarpeta(carpetaId: Int, busqueda: String): Flow<List<com.example.recetarioboliviano.modelo.entidades.Receta>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarRecetaACarpeta(crossRef: RecetaCarpetaCrossRef)

    @Delete
    suspend fun eliminarRecetaDeCarpeta(crossRef: RecetaCarpetaCrossRef)

    @Transaction
    @Query("SELECT * FROM carpetas WHERE id = :carpetaId")
    fun obtenerCarpetaConRecetas(carpetaId: Int): Flow<CarpetaConRecetas>

    @Transaction
    @Query("SELECT * FROM recetas WHERE id = :recetaId")
    fun obtenerCarpetasDeReceta(recetaId: Int): Flow<com.example.recetarioboliviano.modelo.entidades.RecetaConCarpetas>

    @Query("SELECT * FROM recetas WHERE id IN (SELECT recetaId FROM receta_carpeta_cross_ref WHERE carpetaId = :carpetaId)")
    fun obtenerRecetasDeCarpeta(carpetaId: Int): Flow<List<com.example.recetarioboliviano.modelo.entidades.Receta>>
}
