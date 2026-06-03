package com.example.recetarioboliviano.modelo.util

/**
 * Constantes utilizadas en la aplicación.
 */
object Constantes {
    // Departamentos de Bolivia
    val DEPARTAMENTOS_BOLIVIA = listOf(
        "La Paz",
        "Cochabamba",
        "Santa Cruz",
        "Oruro",
        "Potosí",
        "Chuquisaca",
        "Tarija",
        "Beni",
        "Pando"
    )

    // Categorías de recetas
    val CATEGORIAS = listOf(
        "Sopa",
        "Segundo",
        "Postre"
    )

    // Preferencias
    val PREFS_NAME = "recetario_prefs"
    val KEY_USUARIO_REGISTRADO = "usuario_registrado"
    val KEY_USUARIO_ID = "usuario_id"

    // Solicitudes de permisos
    val PERMISSION_REQUEST_CODE = 100
    val CAMERA_PERMISSION_REQUEST_CODE = 101
}
