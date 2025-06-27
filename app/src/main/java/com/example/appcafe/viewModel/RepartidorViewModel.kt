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

data class RepartidorState(
    val pedidos: List<Orden> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RepartidorViewModel @Inject constructor(
    private val ordenesService: OrdenesService
) : ViewModel() {

    private val _pedidosState = MutableStateFlow(RepartidorState())
    val pedidosState: StateFlow<RepartidorState> = _pedidosState.asStateFlow()

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

    fun limpiarError() {
        _pedidosState.value = _pedidosState.value.copy(error = null)
    }
}