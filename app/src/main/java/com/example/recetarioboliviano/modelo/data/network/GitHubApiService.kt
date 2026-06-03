package com.example.recetarioboliviano.modelo.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApiService {

    @GET("BullZMEzequiel/Recetari/master/api/recetas_semanales.json")
    suspend fun obtenerRecetasSemanales(
        @Query("t") cacheBuster: Long   // rompe el caché del CDN
    ): List<RecetaNetwork>

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com/"

        fun create(): GitHubApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Cache-Control", "no-cache")  // refuerzo
                        .build()
                    chain.proceed(request)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApiService::class.java)
        }
    }
}