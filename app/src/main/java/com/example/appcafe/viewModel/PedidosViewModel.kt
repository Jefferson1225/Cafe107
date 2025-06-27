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
                // Usar el Flow del servicio para obtener pedidos en tiempo real
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

    fun actualizarEstadoPedido(pedidoId: String, nuevoEstado: EstadoOrden) {
        viewModelScope.launch {
            try {
                ordenesService.actualizarEstadoOrden(pedidoId, nuevoEstado)

                // El estado se actualizará automáticamente a través del Flow
                // No necesitamos actualizar manualmente el estado local

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

    // Ya no necesitamos este método porque usamos el Flow del servicio
    // que ya proporciona actualizaciones en tiempo real
}