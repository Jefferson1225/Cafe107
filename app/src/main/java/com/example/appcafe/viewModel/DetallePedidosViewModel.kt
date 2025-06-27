package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.OrdenesService
import com.example.appcafe.db.Orden
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetallePedidoState(
    val pedido: Orden? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetallePedidoViewModel @Inject constructor(
    private val ordenesService: OrdenesService
) : ViewModel() {

    private val _pedidoState = MutableStateFlow(DetallePedidoState())
    val pedidoState: StateFlow<DetallePedidoState> = _pedidoState.asStateFlow()

    fun cargarPedido(pedidoId: String) {
        viewModelScope.launch {
            _pedidoState.value = _pedidoState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val pedido = ordenesService.getOrdenById(pedidoId)

                if (pedido != null) {
                    _pedidoState.value = _pedidoState.value.copy(
                        pedido = pedido.copy(id = pedidoId),
                        isLoading = false,
                        error = null
                    )
                } else {
                    _pedidoState.value = _pedidoState.value.copy(
                        pedido = null,
                        isLoading = false,
                        error = "Pedido no encontrado"
                    )
                }
            } catch (e: Exception) {
                _pedidoState.value = _pedidoState.value.copy(
                    pedido = null,
                    isLoading = false,
                    error = e.message ?: "Error al cargar el pedido"
                )
            }
        }
    }

    fun reintentar(pedidoId: String) {
        cargarPedido(pedidoId)
    }
}