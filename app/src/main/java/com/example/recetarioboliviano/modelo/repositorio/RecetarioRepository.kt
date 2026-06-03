package com.example.recetarioboliviano.modelo.repositorio

import com.example.recetarioboliviano.modelo.dao.CarpetaDao
import com.example.recetarioboliviano.modelo.dao.RecetaDao
import com.example.recetarioboliviano.modelo.dao.UsuarioDao
import com.example.recetarioboliviano.modelo.entidades.Carpeta
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.entidades.RecetaCarpetaCrossRef
import com.example.recetarioboliviano.modelo.entidades.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que proporciona acceso a los datos de la aplicación.
 * Sigue el patrón Repository para abstraer las fuentes de datos.
 */
class RecetarioRepository(
    private val usuarioDao: UsuarioDao,
    private val recetaDao: RecetaDao,
    private val carpetaDao: CarpetaDao
) {
    // ================ USUARIO ================
    val usuarioActual: Flow<Usuario?> = usuarioDao.obtenerUsuario()

    suspend fun obtenerUsuarioSync(): Usuario? = usuarioDao.obtenerUsuarioSync()

    suspend fun obtenerUsuarioPorId(id: Int): Usuario? = usuarioDao.obtenerUsuarioPorId(id)

    suspend fun registrarUsuario(usuario: Usuario): Long = usuarioDao.insertar(usuario)

    suspend fun actualizarUsuario(usuario: Usuario) = usuarioDao.actualizar(usuario)

    suspend fun actualizarAvatar(usuarioId: Int, avatarUri: String) =
        usuarioDao.actualizarAvatar(usuarioId, avatarUri)

    // ================ RECETAS ================
    val todasLasRecetas: Flow<List<Receta>> = recetaDao.obtenerTodasLasRecetas()

    val favoritos: Flow<List<Receta>> = recetaDao.obtenerFavoritos()

    val recetasDelUsuario: Flow<List<Receta>> = recetaDao.obtenerRecetasUsuario()

    fun obtenerRecetaPorId(id: Int): Flow<Receta?> = recetaDao.obtenerRecetaPorId(id)

    suspend fun obtenerRecetaPorIdSync(id: Int): Receta? = recetaDao.obtenerRecetaPorIdSync(id)

    fun obtenerRecetasPorDepartamento(departamento: String): Flow<List<Receta>> =
        recetaDao.obtenerRecetasPorDepartamento(departamento)

    fun obtenerRecetasPorCategoria(categoria: String): Flow<List<Receta>> =
        recetaDao.obtenerRecetasPorCategoria(categoria)

    fun buscarRecetas(busqueda: String): Flow<List<Receta>> =
        recetaDao.buscarRecetas(busqueda)

    fun buscarRecetasPorDepartamento(busqueda: String, departamento: String): Flow<List<Receta>> =
        recetaDao.buscarRecetasPorDepartamento(busqueda, departamento)

    fun buscarRecetasPorCategoria(busqueda: String, categoria: String): Flow<List<Receta>> =
        recetaDao.buscarRecetasPorCategoria(busqueda, categoria)

    fun buscarRecetasPorNombreODepartamento(busqueda: String): Flow<List<Receta>> =
        recetaDao.buscarRecetasPorNombreODepartamento(busqueda)

    suspend fun crearReceta(receta: Receta): Long = recetaDao.insertar(receta)

    suspend fun actualizarReceta(receta: Receta) = recetaDao.actualizar(receta)

    suspend fun eliminarReceta(receta: Receta) = recetaDao.eliminar(receta)

    suspend fun toggleFavorito(recetaId: Int, esFavorito: Boolean) =
        recetaDao.actualizarFavorito(recetaId, esFavorito)

    suspend fun contarRecetas(): Int = recetaDao.contarRecetas()

    // ================ CARPETAS ================
    val todasLasCarpetas: Flow<List<Carpeta>> = carpetaDao.obtenerTodasLasCarpetas()

    suspend fun crearCarpeta(nombre: String): Long {
        return carpetaDao.insertarCarpeta(Carpeta(nombre = nombre))
    }

    suspend fun eliminarCarpeta(carpeta: Carpeta) = carpetaDao.eliminarCarpeta(carpeta)

    suspend fun actualizarCarpeta(carpeta: Carpeta) = carpetaDao.actualizarCarpeta(carpeta)

    suspend fun agregarRecetaACarpeta(recetaId: Int, carpetaId: Int) {
        carpetaDao.agregarRecetaACarpeta(RecetaCarpetaCrossRef(recetaId, carpetaId))
    }

    suspend fun eliminarRecetaDeCarpeta(recetaId: Int, carpetaId: Int) {
        carpetaDao.eliminarRecetaDeCarpeta(RecetaCarpetaCrossRef(recetaId, carpetaId))
    }

    fun obtenerRecetasDeCarpeta(carpetaId: Int): Flow<List<Receta>> =
        carpetaDao.obtenerRecetasDeCarpeta(carpetaId)

    fun obtenerCarpetasDeReceta(recetaId: Int) =
        carpetaDao.obtenerCarpetasDeReceta(recetaId)
}
