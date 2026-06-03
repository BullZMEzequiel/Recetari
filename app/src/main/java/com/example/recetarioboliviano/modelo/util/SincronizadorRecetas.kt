package com.example.recetarioboliviano.modelo.util

import android.util.Log
import com.example.recetarioboliviano.modelo.dao.RecetaDao
import com.example.recetarioboliviano.modelo.data.network.GitHubApiService
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clase encargada de sincronizar las recetas locales con el servidor (GitHub).
 *
 * IMPORTANTE: los pasos del servidor ahora se guardan en el campo `preparacion`
 * como un JSON de List<PasoPreparacion>, EXACTAMENTE el mismo formato que generan
 * las recetas creadas por el usuario. Así RecetaDetalleActivity.procesarPasos()
 * lo parsea bien y PasoDetalleAdapter puede mostrar la imagen de cada paso
 * (paso.imagen -> PasoPreparacion.imagenUri).
 */
class SincronizadorRecetas(private val recetaDao: RecetaDao) {

    private val apiService = GitHubApiService.create()
    private val gson = Gson()

    suspend fun sincronizarConServidor() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("Sincronizador", "Iniciando descarga de recetas...")
                val recetasInternet = apiService.obtenerRecetasSemanales(System.currentTimeMillis())
                Log.d("Sincronizador", "Descargadas ${recetasInternet.size} recetas.")

                var nuevasInsertadas = 0
                var actualizadas = 0

                recetasInternet.forEach { recetaNet ->
                    // Buscamos si ya existe por la URL de la imagen de portada (clave única del servidor)
                    val recetaLocal = recetaDao.obtenerRecetaPorUrlServidor(recetaNet.imagen)

                    // 👇 CAMBIO CLAVE: convertimos los pasos del JSON a entidades PasoPreparacion
                    // conservando la URL de la imagen de cada paso, y lo guardamos como JSON.
                    val pasosEntidad = recetaNet.pasos.map { paso ->
                        PasoPreparacion(
                            numero = paso.numero,
                            descripcion = paso.descripcion,
                            imagenUri = paso.imagen   // URL de Cloudinary del paso
                        )
                    }
                    val preparacionJson = gson.toJson(pasosEntidad)

                    if (recetaLocal == null) {
                        Log.d("Sincronizador", "Insertando nueva receta: ${recetaNet.nombre}")
                        val nuevaReceta = Receta(
                            titulo = recetaNet.nombre,
                            imagenUri = recetaNet.imagen,
                            tiempoPreparacion = recetaNet.tiempo ?: "45 min",
                            cantidadPersonas = recetaNet.personas ?: "4 personas",
                            ingredientes = recetaNet.ingredientes,
                            preparacion = preparacionJson,   // 👈 ahora va el JSON con imágenes
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
                                recetaLocal.preparacion != preparacionJson ||   // 👈 comparamos contra el JSON
                                recetaLocal.categoria != (recetaNet.categoria ?: "General") ||
                                recetaLocal.departamento != recetaNet.departamento

                        if (cambioDetectado) {
                            Log.d("Sincronizador", "Actualizando receta existente: ${recetaNet.nombre}")
                            val recetaActualizada = recetaLocal.copy(
                                titulo = recetaNet.nombre,
                                ingredientes = recetaNet.ingredientes,
                                preparacion = preparacionJson,   // 👈 actualizamos con el JSON
                                tiempoPreparacion = recetaNet.tiempo ?: recetaLocal.tiempoPreparacion,
                                cantidadPersonas = recetaNet.personas ?: recetaLocal.cantidadPersonas,
                                categoria = recetaNet.categoria ?: recetaLocal.categoria,
                                departamento = recetaNet.departamento,
                                imagenUri = recetaNet.imagen // Actualizar URL de portada por si cambió
                            )
                            recetaDao.actualizar(recetaActualizada)
                            actualizadas++
                        }
                    }
                }

                Log.d(
                    "Sincronizador",
                    "Sincronización terminada. Nuevas: $nuevasInsertadas, Actualizadas: $actualizadas"
                )
            } catch (e: Exception) {
                Log.e("Sincronizador", "Error sincronizando: ${e.message}", e)
            }
        }
    }
}