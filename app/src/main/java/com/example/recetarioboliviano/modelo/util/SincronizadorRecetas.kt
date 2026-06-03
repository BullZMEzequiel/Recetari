package com.example.recetarioboliviano.modelo.util

import android.util.Log
import com.example.recetarioboliviano.modelo.dao.RecetaDao
import com.example.recetarioboliviano.modelo.data.network.GitHubApiService
import com.example.recetarioboliviano.modelo.entidades.Receta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clase encargada de sincronizar las recetas locales con el servidor (GitHub).
 */
class SincronizadorRecetas(private val recetaDao: RecetaDao) {

    private val apiService = GitHubApiService.create()

    suspend fun sincronizarConServidor() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("Sincronizador", "Iniciando descarga de recetas...")
                val recetasInternet = apiService.obtenerRecetasSemanales()
                Log.d("Sincronizador", "Descargadas ${recetasInternet.size} recetas.")

                var nuevasInsertadas = 0
                var actualizadas = 0

                recetasInternet.forEach { recetaNet ->
                    // Buscamos si ya existe por nombre y departamento (más estable que la URL de imagen)
                    // O mantenemos servidorUrl si confiamos en que no cambia.
                    val recetaLocal = recetaDao.obtenerRecetaPorUrlServidor(recetaNet.imagen)

                    val preparacionFormateada = recetaNet.pasos.joinToString(separator = "\n") { paso ->
                        "${paso.numero}. ${paso.descripcion}"
                    }

                    if (recetaLocal == null) {
                        Log.d("Sincronizador", "Insertando nueva receta: ${recetaNet.nombre}")
                        val nuevaReceta = Receta(
                            titulo = recetaNet.nombre,
                            imagenUri = recetaNet.imagen,
                            tiempoPreparacion = recetaNet.tiempo ?: "45 min",
                            cantidadPersonas = recetaNet.personas ?: "4 personas",
                            ingredientes = recetaNet.ingredientes,
                            preparacion = preparacionFormateada,
                            categoria = recetaNet.categoria ?: "General",
                            departamento = recetaNet.departamento,
                            esFavorito = false,
                            esCreadaPorUsuario = false,
                            servidorUrl = recetaNet.imagen
                        )
                        recetaDao.insertar(nuevaReceta)
                        nuevasInsertadas++
                    } else {
                        // Si ya existe, comparamos campos para ver si hay cambios
                        val cambioDetectado = recetaLocal.titulo != recetaNet.nombre ||
                                recetaLocal.ingredientes != recetaNet.ingredientes ||
                                recetaLocal.preparacion != preparacionFormateada ||
                                recetaLocal.categoria != (recetaNet.categoria ?: "General") ||
                                recetaLocal.departamento != recetaNet.departamento

                        if (cambioDetectado) {
                            Log.d("Sincronizador", "Actualizando receta existente: ${recetaNet.nombre}")
                            val recetaActualizada = recetaLocal.copy(
                                titulo = recetaNet.nombre,
                                ingredientes = recetaNet.ingredientes,
                                preparacion = preparacionFormateada,
                                tiempoPreparacion = recetaNet.tiempo ?: recetaLocal.tiempoPreparacion,
                                cantidadPersonas = recetaNet.personas ?: recetaLocal.cantidadPersonas,
                                categoria = recetaNet.categoria ?: recetaLocal.categoria,
                                departamento = recetaNet.departamento,
                                imagenUri = recetaNet.imagen // Actualizar URL por si cambió
                            )
                            recetaDao.actualizar(recetaActualizada)
                            actualizadas++
                        }
                    }
                }
                Log.d("Sincronizador", "Sincronización terminada. Nuevas: $nuevasInsertadas, Actualizadas: $actualizadas")
            } catch (e: Exception) {
                Log.e("Sincronizador", "Error sincronizando: ${e.message}", e)
            }
        }
    }
}
