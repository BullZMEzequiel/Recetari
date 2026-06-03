package com.example.recetarioboliviano.modelo.dao

import androidx.room.*
import com.example.recetarioboliviano.modelo.entidades.Receta
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad Receta.
 */
@Dao
interface RecetaDao {
    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    fun obtenerTodasLasRecetas(): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE id = :id")
    fun obtenerRecetaPorId(id: Int): Flow<Receta?>

    @Query("SELECT * FROM recetas WHERE id = :id")
    suspend fun obtenerRecetaPorIdSync(id: Int): Receta?

    @Query("SELECT * FROM recetas WHERE departamento = :departamento ORDER BY titulo ASC")
    fun obtenerRecetasPorDepartamento(departamento: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE categoria = :categoria ORDER BY titulo ASC")
    fun obtenerRecetasPorCategoria(categoria: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE esFavorito = 1 ORDER BY titulo ASC")
    fun obtenerFavoritos(): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE titulo LIKE '%' || :busqueda || '%' ORDER BY titulo ASC")
    fun buscarRecetas(busqueda: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE titulo LIKE '%' || :busqueda || '%' AND departamento = :departamento ORDER BY titulo ASC")
    fun buscarRecetasPorDepartamento(busqueda: String, departamento: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE titulo LIKE '%' || :busqueda || '%' AND categoria = :categoria ORDER BY titulo ASC")
    fun buscarRecetasPorCategoria(busqueda: String, categoria: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE titulo LIKE '%' || :busqueda || '%' AND departamento = :departamento AND categoria = :categoria ORDER BY titulo ASC")
    fun buscarRecetasPorDeptoYCategoria(busqueda: String, departamento: String, categoria: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE departamento = :departamento AND categoria = :categoria ORDER BY titulo ASC")
    fun obtenerRecetasPorDeptoYCategoria(departamento: String, categoria: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE esFavorito = 1 AND (titulo LIKE '%' || :busqueda || '%' OR ingredientes LIKE '%' || :busqueda || '%') ORDER BY titulo ASC")
    fun buscarFavoritos(busqueda: String): Flow<List<Receta>>

    // Buscar por nombre de receta o departamento
    @Query("SELECT * FROM recetas WHERE titulo LIKE '%' || :busqueda || '%' OR departamento LIKE '%' || :busqueda || '%' ORDER BY titulo ASC")
    fun buscarRecetasPorNombreODepartamento(busqueda: String): Flow<List<Receta>>

    @Query("SELECT * FROM recetas WHERE esCreadaPorUsuario = 1 ORDER BY fechaCreacion DESC")
    fun obtenerRecetasUsuario(): Flow<List<Receta>>

    // 1. Cambiamos la estrategia a IGNORE para que no pise los datos guardados ni favoritos del usuario
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(receta: Receta): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarVarias(recetas: List<Receta>)

    @Update
    suspend fun actualizar(receta: Receta)

    @Delete
    suspend fun eliminar(receta: Receta)

    @Query("UPDATE recetas SET esFavorito = :esFavorito WHERE id = :recetaId")
    suspend fun actualizarFavorito(recetaId: Int, esFavorito: Boolean)

    @Query("SELECT COUNT(*) FROM recetas")
    suspend fun contarRecetas(): Int

    // 2. Agregamos estas queries para el servidor
    @Query("SELECT EXISTS(SELECT 1 FROM recetas WHERE servidorUrl = :url LIMIT 1)")
    suspend fun existeRecetaServidor(url: String): Boolean

    @Query("SELECT * FROM recetas WHERE servidorUrl = :url LIMIT 1")
    suspend fun obtenerRecetaPorUrlServidor(url: String): Receta?
}
