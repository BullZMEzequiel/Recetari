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
    private val _busquedaCarpeta = MutableLiveData<String>("")
    val todasLasCarpetas: LiveData<List<Carpeta>> = _busquedaCarpeta.switchMap { busqueda ->
        if (busqueda.isNullOrBlank()) {
            repository.todasLasCarpetas.asLiveData()
        } else {
            repository.buscarCarpetas(busqueda).asLiveData()
        }
    }

    fun buscarCarpetas(busqueda: String) {
        _busquedaCarpeta.value = busqueda
    }

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
     * Obtiene las recetas contenidas en una carpeta con soporte para búsqueda.
     */
    private val _busquedaEnCarpeta = MutableLiveData<String>("")
    private val _idCarpetaActual = MutableLiveData<Int>()

    val recetasDeCarpeta: LiveData<List<Receta>> = MediatorLiveData<List<Receta>>().apply {
        addSource(_idCarpetaActual) { id ->
            id?.let {
                val busqueda = _busquedaEnCarpeta.value ?: ""
                if (busqueda.isBlank()) {
                    addSource(repository.obtenerRecetasDeCarpeta(it).asLiveData()) { value = it }
                } else {
                    addSource(repository.buscarRecetasDeCarpeta(it, busqueda).asLiveData()) { value = it }
                }
            }
        }
        addSource(_busquedaEnCarpeta) { busqueda ->
            _idCarpetaActual.value?.let { id ->
                if (busqueda.isNullOrBlank()) {
                    addSource(repository.obtenerRecetasDeCarpeta(id).asLiveData()) { value = it }
                } else {
                    addSource(repository.buscarRecetasDeCarpeta(id, busqueda).asLiveData()) { value = it }
                }
            }
        }
    }

    fun setCarpetaActual(id: Int) {
        _idCarpetaActual.value = id
    }

    fun buscarEnCarpeta(busqueda: String) {
        _busquedaEnCarpeta.value = busqueda
    }

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
