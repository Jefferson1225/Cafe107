package com.example.appcare.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.Services.CarritoService
import com.example.appcafe.Services.DireccionesService
import com.example.appcafe.Services.OrdenesService
import com.example.appcafe.db.Direccion
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.db.MetodoPago
import com.example.appcafe.db.Orden
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CarritoViewModel @Inject constructor(
    private val carritoService: CarritoService,
    private val direccionesService: DireccionesService,
    private val ordenesService: OrdenesService
) : ViewModel() {

    private val _carritoState = MutableStateFlow(CarritoUIState())
    val carritoState: StateFlow<CarritoUIState> = _carritoState.asStateFlow()

    private val _direcciones = MutableStateFlow<List<Direccion>>(emptyList())
    val direcciones: StateFlow<List<Direccion>> = _direcciones.asStateFlow()

    init {
        cargarCarrito()
        // Nota: cargarDirecciones() ahora necesita el userId
        // Deberías llamarlo cuando tengas el userId disponible
    }

    private fun cargarCarrito() {
        viewModelScope.launch {
            try {
                carritoService.getCarrito().collect { carrito ->
                    _carritoState.value = _carritoState.value.copy(
                        items = carrito?.items ?: emptyList(),
                        subtotal = carrito?.subtotal ?: 0.0,
                        total = carrito?.total ?: 0.0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun cargarDirecciones(userId: String) {
        viewModelScope.launch {
            try {
                direccionesService.getDirecciones(userId).collect { direcciones ->
                    _direcciones.value = direcciones
                    // Seleccionar dirección principal por defecto
                    val direccionPrincipal = direcciones.find { it.esPrincipal }
                    if (direccionPrincipal != null && _carritoState.value.direccionSeleccionada == null) {
                        _carritoState.value = _carritoState.value.copy(
                            direccionSeleccionada = direccionPrincipal
                        )
                    }
                }
            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(error = e.message)
            }
        }
    }

    fun actualizarCantidad(itemId: String, nuevaCantidad: Int) {
        viewModelScope.launch {
            try {
                carritoService.actualizarCantidad(itemId, nuevaCantidad)
            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(error = e.message)
            }
        }
    }

    fun eliminarItem(itemId: String) {
        viewModelScope.launch {
            try {
                carritoService.eliminarItem(itemId)
            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(error = e.message)
            }
        }
    }

    fun seleccionarDireccion(direccion: Direccion) {
        _carritoState.value = _carritoState.value.copy(
            direccionSeleccionada = direccion
        )
    }

    fun seleccionarMetodoPago(metodo: MetodoPago) {
        _carritoState.value = _carritoState.value.copy(
            metodoPagoSeleccionado = metodo
        )
    }

    fun crearOrden() {
        viewModelScope.launch {
            try {
                val currentState = _carritoState.value
                val orden = Orden(
                    items = currentState.items,
                    subtotal = currentState.subtotal,
                    total = currentState.total,
                    direccionEntrega = currentState.direccionSeleccionada ?: return@launch,
                    metodoPago = currentState.metodoPagoSeleccionado?.displayName ?: return@launch,
                    estado = EstadoOrden.PENDIENTE
                )

                ordenesService.crearOrden(orden)
                carritoService.limpiarCarrito()

                _carritoState.value = _carritoState.value.copy(
                    ordenCreada = true
                )

            } catch (e: Exception) {
                _carritoState.value = _carritoState.value.copy(error = e.message)
            }
        }
    }

    fun limpiarError() {
        _carritoState.value = _carritoState.value.copy(error = null)
    }
}

data class CarritoUIState(
    val items: List<ItemCarrito> = emptyList(),
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val direccionSeleccionada: Direccion? = null,
    val metodoPagoSeleccionado: MetodoPago? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val ordenCreada: Boolean = false
)