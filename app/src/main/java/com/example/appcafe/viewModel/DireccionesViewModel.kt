package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.DireccionesService
import com.example.appcafe.db.Direccion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DireccionesViewModel @Inject constructor(
    private val direccionesService: DireccionesService
) : ViewModel() {

    fun agregarDireccion(
        userId: String,
        nombre: String,
        descripcion: String,
        icono: String,
        esPrincipal: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            direccionesService.agregarDireccion(
                userId,
                nombre,
                descripcion,
                icono,
                esPrincipal,
                onSuccess,
                onError
            )
        }
    }

    fun actualizarDireccion(
        userId: String,
        direccionAntigua: Direccion,
        direccionNueva: Direccion,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            direccionesService.actualizarDireccion(
                userId,
                direccionAntigua,
                direccionNueva,
                onSuccess,
                onError
            )
        }
    }

    fun eliminarDireccion(
        userId: String,
        direccion: Direccion,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            direccionesService.eliminarDireccion(
                userId,
                direccion,
                onSuccess,
                onError
            )
        }
    }
}
