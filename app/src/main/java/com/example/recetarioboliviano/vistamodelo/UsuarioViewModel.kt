package com.example.recetarioboliviano.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.modelo.entidades.Usuario
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la información del usuario.
 */
class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RecetarioApp).repository

    val usuarioActual: LiveData<Usuario?> = repository.usuarioActual.asLiveData()

    fun registrarUsuario(usuario: Usuario, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val usuarioExistente = repository.obtenerUsuarioSync()
                if (usuarioExistente != null) {
                    onComplete(false, "Ya existe un usuario registrado")
                    return@launch
                }
                val id = repository.registrarUsuario(usuario)
                if (id > 0) {
                    onComplete(true, null)
                } else {
                    onComplete(false, "Error al registrar usuario")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error desconocido")
            }
        }
    }

    fun actualizarUsuario(usuario: Usuario, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.actualizarUsuario(usuario)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al actualizar usuario")
            }
        }
    }

    fun actualizarAvatar(usuarioId: Int, avatarUri: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.actualizarAvatar(usuarioId, avatarUri)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al actualizar avatar")
            }
        }
    }
}
