package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.OrdenesService
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PedidosState(
    val pedidos: List<Orden> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PedidosViewModel @Inject constructor(
    private val ordenesService: OrdenesService
) : ViewModel() {

    private val _pedidosState = MutableStateFlow(PedidosState())
    val pedidosState: StateFlow<PedidosState> = _pedidosState.asStateFlow()

    fun cargarPedidos(userId: String) {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null)

            try {
                ordenesService.getOrdenes()
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

    // MÉTODOS FALTANTES PARA REPARTIDORES:

    fun cargarPedidosPorEstado(estado: EstadoOrden, onResult: (List<Orden>) -> Unit) {
        viewModelScope.launch {
            try {
                ordenesService.getOrdenesPorEstado(estado)
                    .catch { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            error = "Error al cargar pedidos: ${exception.message}"
                        )
                    }
                    .collect { pedidos ->
                        onResult(pedidos)
                    }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al cargar pedidos por estado: ${e.message}"
                )
            }
        }
    }


    fun cargarPedidosDeRepartidor(repartidorId: String, onResult: (List<Orden>) -> Unit) {
        viewModelScope.launch {
            try {
                ordenesService.getOrdenesPorRepartidor(repartidorId)
                    .catch { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            error = "Error al cargar pedidos: ${exception.message}"
                        )
                    }
                    .collect { pedidos ->
                        val pedidosEnCamino = pedidos.filter { it.estado == EstadoOrden.EN_CAMINO }
                        onResult(pedidosEnCamino)
                    }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al cargar pedidos del repartidor: ${e.message}"
                )
            }
        }
    }


    fun asignarRepartidor(
        pedidoId: String,
        repartidorId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Asignar repartidor y cambiar estado a EN_CAMINO
                ordenesService.asignarRepartidor(pedidoId, repartidorId)
                ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.EN_CAMINO)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al asignar repartidor: ${e.message}")
            }
        }
    }


    fun marcarComoEntregado(
        pedidoId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.ENTREGADO)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al marcar como entregado: ${e.message}")
            }
        }
    }

    fun actualizarEstadoPedido(pedidoId: String, nuevoEstado: EstadoOrden) {
        viewModelScope.launch {
            try {
                ordenesService.actualizarEstadoOrden(pedidoId, nuevoEstado)
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al actualizar pedido: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _pedidosState.value = _pedidosState.value.copy(error = null)
    }
}