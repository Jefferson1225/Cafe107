package com.example.appcafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.db.Orden
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PedidosState(
    val pedidos: List<Orden> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PedidosViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _pedidosState = MutableStateFlow(PedidosState())
    val pedidosState: StateFlow<PedidosState> = _pedidosState.asStateFlow()

    fun cargarPedidos(userId: String) {
        viewModelScope.launch {
            _pedidosState.value = _pedidosState.value.copy(isLoading = true, error = null)

            try {
                firestore.collection("ordenes")
                    .whereEqualTo("usuarioId", userId)
                    .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val pedidos = documents.mapNotNull { document ->
                            try {
                                document.toObject(Orden::class.java).copy(id = document.id)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        _pedidosState.value = _pedidosState.value.copy(
                            pedidos = pedidos,
                            isLoading = false,
                            error = null
                        )
                    }
                    .addOnFailureListener { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            isLoading = false,
                            error = "Error al cargar pedidos: ${exception.message}"
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

    fun actualizarEstadoPedido(pedidoId: String, nuevoEstado: com.example.appcafe.db.EstadoOrden) {
        viewModelScope.launch {
            try {
                firestore.collection("ordenes")
                    .document(pedidoId)
                    .update("estado", nuevoEstado.name)
                    .addOnSuccessListener {
                        // Actualizar el estado local
                        val pedidosActualizados = _pedidosState.value.pedidos.map { pedido ->
                            if (pedido.id == pedidoId) {
                                pedido.copy(estado = nuevoEstado)
                            } else {
                                pedido
                            }
                        }

                        _pedidosState.value = _pedidosState.value.copy(
                            pedidos = pedidosActualizados
                        )
                    }
                    .addOnFailureListener { exception ->
                        _pedidosState.value = _pedidosState.value.copy(
                            error = "Error al actualizar pedido: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                _pedidosState.value = _pedidosState.value.copy(
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _pedidosState.value = _pedidosState.value.copy(error = null)
    }

    // Función para escuchar cambios en tiempo real (también modificada temporalmente)
    fun escucharPedidosEnTiempoReal(userId: String) {
        firestore.collection("ordenes")
            .whereEqualTo("usuarioId", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _pedidosState.value = _pedidosState.value.copy(
                        isLoading = false,
                        error = "Error al escuchar cambios: ${error.message}"
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val pedidos = snapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(Orden::class.java)?.copy(id = document.id)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _pedidosState.value = _pedidosState.value.copy(
                        pedidos = pedidos,
                        isLoading = false,
                        error = null
                    )
                }
            }
    }
}