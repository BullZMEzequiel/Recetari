package com.example.recetarioboliviano.modelo.util

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
                // 1. Descargamos las recetas desde GitHub
                val recetasInternet = apiService.obtenerRecetasSemanales()
                var nuevasInsertadas = 0

                // 2. Procesamos una por una para verificar duplicados y respetar favoritos locales
                recetasInternet.forEach { recetaNet ->
                    // Usamos la URL de la imagen como identificador único del servidor
                    val yaExiste = recetaDao.existeRecetaServidor(recetaNet.imagen)

                    if (!yaExiste) {
                        // En lugar de meter el JSON crudo que rompe la UI, unificamos los pasos en un lindo texto legible
                        val preparacionFormateada = recetaNet.pasos.joinToString(separator = "\n") { paso ->
                            "${paso.numero}. ${paso.descripcion}"
                        }
                        
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
                            esCreadaPorUsuario = false, // Oficial del sistema
                            servidorUrl = recetaNet.imagen // Clave de control
                        )
                        
                        recetaDao.insertar(nuevaReceta)
                        nuevasInsertadas++
                    }
                }
                
                println("Sincronización completada con éxito. Se añadieron $nuevasInsertadas recetas nuevas.")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Fallo de red o servidor offline. Trabajando con caché previa de Room.")
            }
        }
    }
}
