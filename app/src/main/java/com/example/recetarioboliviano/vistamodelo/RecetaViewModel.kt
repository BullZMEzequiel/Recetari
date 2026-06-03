package com.example.recetarioboliviano.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.modelo.entidades.Receta
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las recetas.
 */
class RecetaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RecetarioApp).repository

    // LiveData para todas las recetas
    val todasLasRecetas: LiveData<List<Receta>> = repository.todasLasRecetas.asLiveData()

    // LiveData para favoritos
    val favoritos: LiveData<List<Receta>> = repository.favoritos.asLiveData()

    // LiveData para recetas del usuario
    val recetasDelUsuario: LiveData<List<Receta>> = repository.recetasDelUsuario.asLiveData()

    // Búsqueda y filtros
    private val _busquedaActual = MutableLiveData<String>("")
    private val _departamentoFiltro = MutableLiveData<String?>(null)
    private val _categoriaFiltro = MutableLiveData<String?>(null)
    private val _verSoloFavoritos = MutableLiveData<Boolean>(false)

    // Trigger para combinar todos los filtros
    private val triggerFiltros = MediatorLiveData<Unit>().apply {
        addSource(_busquedaActual) { value = Unit }
        addSource(_departamentoFiltro) { value = Unit }
        addSource(_categoriaFiltro) { value = Unit }
        addSource(_verSoloFavoritos) { value = Unit }
    }

    val recetasFiltradas: LiveData<List<Receta>> = triggerFiltros.switchMap {
        val busqueda = _busquedaActual.value ?: ""
        val departamento = _departamentoFiltro.value
        val categoria = _categoriaFiltro.value
        val soloFavoritos = _verSoloFavoritos.value ?: false

        if (soloFavoritos) {
            repository.favoritos.asLiveData()
        } else {
            when {
                busqueda.isNotEmpty() && departamento != null -> {
                    repository.buscarRecetasPorDepartamento(busqueda, departamento).asLiveData()
                }
                busqueda.isNotEmpty() && categoria != null -> {
                    repository.buscarRecetasPorCategoria(busqueda, categoria).asLiveData()
                }
                busqueda.isNotEmpty() -> {
                    repository.buscarRecetasPorNombreODepartamento(busqueda).asLiveData()
                }
                departamento != null -> {
                    repository.obtenerRecetasPorDepartamento(departamento).asLiveData()
                }
                categoria != null -> {
                    repository.obtenerRecetasPorCategoria(categoria).asLiveData()
                }
                else -> {
                    repository.todasLasRecetas.asLiveData()
                }
            }
        }
    }

    // Receta seleccionada
    private val _recetaSeleccionada = MutableLiveData<Receta?>()
    val recetaSeleccionada: LiveData<Receta?> = _recetaSeleccionada

    fun seleccionarReceta(receta: Receta) {
        _recetaSeleccionada.value = receta
    }

    fun limpiarSeleccion() {
        _recetaSeleccionada.value = null
    }

    // Búsqueda
    fun buscar(busqueda: String) {
        _busquedaActual.value = busqueda
    }

    // Buscar por nombre de receta O departamento
    fun buscarRecetasPorNombreODepartamento(busqueda: String) {
        _busquedaActual.value = busqueda
        _departamentoFiltro.value = null
        _categoriaFiltro.value = null
    }

    fun filtrarPorDepartamento(departamento: String?) {
        _departamentoFiltro.value = departamento
        if (departamento != null) {
            _categoriaFiltro.value = null
        }
    }

    fun filtrarPorCategoria(categoria: String?) {
        _categoriaFiltro.value = categoria
        if (categoria != null) {
            _departamentoFiltro.value = null
        }
    }

    fun limpiarFiltros() {
        _busquedaActual.value = ""
        _departamentoFiltro.value = null
        _categoriaFiltro.value = null
        _verSoloFavoritos.value = false // <--- ESTO ES LO QUE FALTABA
    }

    // CRUD
    fun crearReceta(receta: Receta, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repository.crearReceta(receta.copy(esCreadaPorUsuario = true))
                if (id > 0) {
                    onComplete(true, null)
                } else {
                    onComplete(false, "Error al crear receta")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error desconocido")
            }
        }
    }

    fun actualizarReceta(receta: Receta, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.actualizarReceta(receta)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error desconocido")
            }
        }
    }

    fun eliminarReceta(receta: Receta, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.eliminarReceta(receta)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error desconocido")
            }
        }
    }

    fun toggleFavorito(receta: Receta) {
        viewModelScope.launch {
            repository.toggleFavorito(receta.id, !receta.esFavorito)
        }
    }

    fun obtenerRecetaPorId(id: Int): LiveData<Receta?> {
        return repository.obtenerRecetaPorId(id).asLiveData()
    }

    // Lógica para las pestañas (Tabs)
    fun mostrarTodas() {
        _verSoloFavoritos.value = false
        limpiarFiltros()
    }

    fun mostrarFavoritos() {
        _verSoloFavoritos.value = true
        _busquedaActual.value = "" // Opcional: limpiar búsqueda al ir a favoritos
    }
}
