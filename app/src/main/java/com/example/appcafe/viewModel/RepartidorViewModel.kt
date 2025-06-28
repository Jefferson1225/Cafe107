package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.OrdenesService
import com.example.appcafe.Services.RepartidorService
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import com.example.appcafe.db.Repartidor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepartidorState(
    val pedidos: List<Orden> = emptyList(),
    val repartidores: List<Repartidor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false
)

@HiltViewModel
class RepartidorViewModel @Inject constructor(
    private val ordenesService: OrdenesService,
    private val repartidorService: RepartidorService // Necesitarás crear este servicio
) : ViewModel() {

    private val _pedidosState = MutableStateFlow(RepartidorState())
    val pedidosState: StateFlow<RepartidorState> = _pedidosState.asStateFlow()

    // Propiedades para acceder a estados individuales
    val isLoading: StateFlow<Boolean> = _pedidosState.map { it.isLoading }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    val errorMessage: StateFlow<String?> = _pedidosState.map { it.error }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    val operacionExitosa: StateFlow<Boolean> = _pedidosState.map { it.operacionExitosa }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    fun cargarPedidosParaRepartidor() {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null)

            try {
                ordenesService.getPedidosParaRepartidor()
                    .catch { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            isLoading = false,
                            error = "Error al cargar pedidos: ${exception.message}"
                        )
                    }
                    .collect { pedidos ->
                        _pedidosState.value = _pedidosState.value.copy(
                            pedidos = pedidos,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun agregarRepartidor(repartidor: Repartidor) {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null, operacionExitosa = false)

            try {
                repartidorService.agregarRepartidor(repartidor)
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    error = null,
                    operacionExitosa = true
                )
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    error = "Error al agregar repartidor: ${e.message}",
                    operacionExitosa = false
                )
            }
        }
    }

    fun cargarRepartidores() {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null)

            try {
                repartidorService.getAllRepartidores()
                    .catch { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            isLoading = false,
                            error = "Error al cargar repartidores: ${exception.message}"
                        )
                    }
                    .collect { repartidores ->
                        _pedidosState.value = _pedidosState.value.copy(
                            repartidores = repartidores,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun aceptarPedido(pedidoId: String, repartidorId: String) {
        viewModelScope.launch {
            try {
                ordenesService.asignarRepartidor(pedidoId, repartidorId)
                ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.EN_CAMINO)
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al aceptar pedido: ${e.message}"
                )
            }
        }
    }

    fun marcarComoEntregado(pedidoId: String) {
        viewModelScope.launch {
            try {
                ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.ENTREGADO)
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al marcar como entregado: ${e.message}"
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
            error = null,
            operacionExitosa = false
        )
    }
}