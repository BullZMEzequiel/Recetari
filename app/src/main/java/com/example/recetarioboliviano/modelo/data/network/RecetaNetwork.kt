package com.example.recetarioboliviano.modelo.data.network

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para recibir recetas desde la API (GitHub/Cloudinary).
 * Representa la estructura del JSON externo.
 */
data class RecetaNetwork(
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("departamento")
    val departamento: String,
    
    @SerializedName("ingredientes")
    val ingredientes: String,
    
    @SerializedName("preparacion")
    val preparacion: String,
    
    @SerializedName("imagen")
    val imagen: String,
    
    @SerializedName("pasos")
    val pasos: List<PasoNetwork>,
    
    @SerializedName("categoria")
    val categoria: String? = "General",
    
    @SerializedName("tiempo")
    val tiempo: String? = "30 min",
    
    @SerializedName("personas")
    val personas: String? = "2 personas"
)

data class PasoNetwork(
    @SerializedName("numeroPaso")
    val numero: Int,
    
    @SerializedName("descripcion")
    val descripcion: String,
    
    @SerializedName("imagen")
    val imagen: String? = null
)
