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

data class CocinaState(
    val pedidos: List<Orden> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CocinaViewModel @Inject constructor(
    private val ordenesService: OrdenesService
) : ViewModel() {

    private val _pedidosState = MutableStateFlow(CocinaState())
    val pedidosState: StateFlow<CocinaState> = _pedidosState.asStateFlow()

    fun cargarPedidosParaCocina() {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null)

            try {
                ordenesService.getPedidosParaCocina()
                    .catch { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            isLoading = false,
                            error = "Error al cargar pedidos: ${exception.message}"
                        )
                    }
                    .collect { pedidos ->
                        // Filtrar solo pedidos que necesita ver la cocina
                        val pedidosCocina = pedidos.filter { pedido ->
                            pedido.estado in listOf(
                                EstadoOrden.PENDIENTE,
                                EstadoOrden.CONFIRMADO,
                                EstadoOrden.EN_PREPARACION
                            )
                        }

                        _pedidosState.value = _pedidosState.value.copy(
                            pedidos = pedidosCocina,
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

    fun confirmarPedido(pedidoId: String) {
        viewModelScope.launch {
            try {
                // Cambiar de PENDIENTE a CONFIRMADO
                ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.CONFIRMADO)

                // Recargar pedidos para reflejar el cambio
                cargarPedidosParaCocina()
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al confirmar pedido: ${e.message}"
                )
            }
        }
    }


    fun pasarPedidoARepartidor(pedidoId: String) {
        viewModelScope.launch {
            try {
                val orden = ordenesService.getOrdenById(pedidoId)

                when (orden?.estado) {
                    EstadoOrden.CONFIRMADO -> {
                        // Pasa de CONFIRMADO -> EN_PREPARACION
                        ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.EN_PREPARACION)
                    }

                    EstadoOrden.EN_PREPARACION -> {
                        // Pasa de EN_PREPARACION -> ESPERANDO_REPARTIDOR
                        ordenesService.actualizarEstadoOrden(pedidoId, EstadoOrden.ESPERANDO_REPARTIDOR)
                    }

                    else -> {
                        _pedidosState.value = _pedidosState.value.copy(
                            error = "No se puede pasar a repartidor desde el estado ${orden?.estado}"
                        )
                        return@launch
                    }
                }

                cargarPedidosParaCocina()
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error al pasar pedido a repartidor: ${e.message}"
                )
            }
        }
    }


    fun limpiarError() {
        _pedidosState.value = _pedidosState.value.copy(error = null)
    }
}