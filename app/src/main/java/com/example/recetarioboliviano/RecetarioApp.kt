package com.example.recetarioboliviano

import android.app.Application
import com.example.recetarioboliviano.modelo.data.base_datos.AppDatabase
import com.example.recetarioboliviano.modelo.repositorio.RecetarioRepository

/**
 * Clase Application para la aplicación Recetario Boliviano.
 * Inicializa la base de datos y el repositorio.
 */
class RecetarioApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: RecetarioRepository by lazy {
        RecetarioRepository(
            database.usuarioDao(),
            database.recetaDao(),
            database.carpetaDao()
        )
    }

    companion object {
        private lateinit var instance: RecetarioApp

        fun getInstance(): RecetarioApp = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
