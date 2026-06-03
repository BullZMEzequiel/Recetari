package com.example.recetarioboliviano.vistamodelo

import androidx.lifecycle.*
import com.example.recetarioboliviano.modelo.entidades.Carpeta
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.repositorio.RecetarioRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las carpetas y su contenido.
 */
class CarpetaViewModel(private val repository: RecetarioRepository) : ViewModel() {

    // Todas las carpetas disponibles
    val todasLasCarpetas: LiveData<List<Carpeta>> = repository.todasLasCarpetas.asLiveData()

    /**
     * Crea una nueva carpeta con el nombre proporcionado.
     */
    fun crearCarpeta(nombre: String) = viewModelScope.launch {
        repository.crearCarpeta(nombre)
    }

    /**
     * Elimina una carpeta existente.
     */
    fun eliminarCarpeta(carpeta: Carpeta) = viewModelScope.launch {
        repository.eliminarCarpeta(carpeta)
    }

    /**
     * Actualiza el nombre de una carpeta.
     */
    fun actualizarCarpeta(carpeta: Carpeta) = viewModelScope.launch {
        repository.actualizarCarpeta(carpeta)
    }

    /**
     * Agrega una receta a una carpeta específica.
     */
    fun agregarRecetaACarpeta(recetaId: Int, carpetaId: Int) = viewModelScope.launch {
        repository.agregarRecetaACarpeta(recetaId, carpetaId)
    }

    /**
     * Elimina una receta de una carpeta.
     */
    fun eliminarRecetaDeCarpeta(recetaId: Int, carpetaId: Int) = viewModelScope.launch {
        repository.eliminarRecetaDeCarpeta(recetaId, carpetaId)
    }

    /**
     * Obtiene las recetas contenidas en una carpeta.
     */
    fun obtenerRecetasDeCarpeta(carpetaId: Int): LiveData<List<Receta>> {
        return repository.obtenerRecetasDeCarpeta(carpetaId).asLiveData()
    }

    /**
     * Obtiene las carpetas a las que pertenece una receta.
     */
    fun obtenerCarpetasDeReceta(recetaId: Int) =
        repository.obtenerCarpetasDeReceta(recetaId).asLiveData()
}

/**
 * Factory para crear el CarpetaViewModel con el repositorio.
 */
class CarpetaViewModelFactory(private val repository: RecetarioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarpetaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarpetaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
