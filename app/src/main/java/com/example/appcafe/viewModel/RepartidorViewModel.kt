package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.RepartidorService
import com.example.appcafe.db.Repartidor
import com.example.cafeteria.db.AuthService // Importar AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepartidorViewModel @Inject constructor(
    private val repartidorService: RepartidorService,
    private val authService: AuthService // Inyectar AuthService
) : ViewModel() {

    private val _pedidosState = MutableStateFlow(RepartidorState())
    val pedidosState: StateFlow<RepartidorState> = _pedidosState.asStateFlow()

    private val _repartidores = MutableStateFlow<List<Repartidor>>(emptyList())
    val repartidores: StateFlow<List<Repartidor>> = _repartidores.asStateFlow()

    init {
        cargarRepartidores()
    }

    // MÉTODO ACTUALIZADO: Ahora crea cuenta de autenticación Y guarda en repartidores
    fun agregarRepartidor(repartidor: Repartidor) {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null)

            try {
                // Usar el nuevo método del AuthService que crea la cuenta de autenticación
                val resultado = authService.registrarRepartidor(
                    nombre = repartidor.nombre,
                    apellidos = repartidor.apellidos,
                    correo = repartidor.correo,
                    contraseña = repartidor.contraseña,
                    telefono = repartidor.telefono,
                    fotoUrl = repartidor.fotoUrl,
                    disponible = repartidor.disponible
                )

                if (resultado.isSuccess) {
                    _pedidosState.value = _pedidosState.value.copy(
                        isLoading = false,
                        operacionExitosa = true,
                        error = null
                    )
                    // Recargar la lista de repartidores
                    cargarRepartidores()
                } else {
                    val error = resultado.exceptionOrNull()?.message ?: "Error desconocido al registrar repartidor"
                    _pedidosState.value = _pedidosState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    error = "Error al agregar repartidor: ${e.message}"
                )
            }
        }
    }

    private fun cargarRepartidores() {
        viewModelScope.launch {
            try {
                repartidorService.getAllRepartidores().collect { lista ->
                    _repartidores.value = lista
                }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al cargar repartidores: ${e.message}"
                )
            }
        }
    }

    fun actualizarDisponibilidad(repartidorId: String, disponible: Boolean) {
        viewModelScope.launch {
            try {
                repartidorService.actualizarDisponibilidad(repartidorId, disponible)
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al actualizar disponibilidad: ${e.message}"
                )
            }
        }
    }

    fun eliminarRepartidor(repartidorId: String) {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true)
            try {
                repartidorService.eliminarRepartidor(repartidorId)
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    operacionExitosa = true
                )
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    error = "Error al eliminar repartidor: ${e.message}"
                )
            }
        }
    }

    fun mostrarError(mensaje: String) {
        _pedidosState.value = _pedidosState.value.copy(error = mensaje)
    }

    fun limpiarError() {
        _pedidosState.value = _pedidosState.value.copy(error = null)
    }

    fun limpiarEstados() {
        _pedidosState.value = _pedidosState.value.copy(
            isLoading = false,
            error = null,
            operacionExitosa = false
        )
    }
}

data class RepartidorState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false
)