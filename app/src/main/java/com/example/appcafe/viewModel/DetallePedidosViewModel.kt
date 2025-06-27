package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.OrdenesService
import com.example.appcafe.db.Orden
import com.example.appcafe.db.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetallePedidoState(
    val pedido: Orden? = null,
    val cliente: Usuario? = null,
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
            _pedidoState.value = _pedidoState.value.copy(isLoading = true, error = null)

            try {
                val (pedido, cliente) = ordenesService.getOrdenConCliente(pedidoId)

                if (pedido != null) {
                    _pedidoState.value = _pedidoState.value.copy(
                        pedido = pedido,
                        cliente = cliente,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _pedidoState.value = _pedidoState.value.copy(
                        isLoading = false,
                        error = "Pedido no encontrado"
                    )
                }
            } catch (e: Exception) {
                _pedidoState.value = _pedidoState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el pedido: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _pedidoState.value = _pedidoState.value.copy(error = null)
    }
}