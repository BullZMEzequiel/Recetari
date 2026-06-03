package com.example.recetarioboliviano.modelo.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface GitHubApiService {

    // Apunta directamente a la ruta de tu archivo en la rama principal
    @GET("BullZMEzequiel/Recetari/master/api/recetas_semanales.json")
    suspend fun obtenerRecetasSemanales(): List<RecetaNetwork>

    companion object {
        // La URL Base de GitHub para archivos planos en crudo (Raw)
        private const val BASE_URL = "https://raw.githubusercontent.com/"

        fun create(): GitHubApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApiService::class.java)
        }
    }
}