package com.example.appcafe.Services

import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidosService @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("ordenes")

    fun crearPedido(
        orden: Orden,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .add(orden)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun obtenerPedidos(
        userId: String,
        onSuccess: (List<Orden>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
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
                onSuccess(pedidos)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun obtenerPedidoPorId(
        pedidoId: String,
        onSuccess: (Orden?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .document(pedidoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val orden = document.toObject(Orden::class.java)?.copy(id = document.id)
                        onSuccess(orden)
                    } catch (e: Exception) {
                        onError(e)
                    }
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun actualizarEstadoPedido(
        pedidoId: String,
        nuevoEstado: EstadoOrden,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .document(pedidoId)
            .update("estado", nuevoEstado.name)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun actualizarFechaEntrega(
        pedidoId: String,
        fechaEntrega: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .document(pedidoId)
            .update("fechaEntregaEstimada", fechaEntrega)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun cancelarPedido(
        pedidoId: String,
        motivoCancelacion: String = "",
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val updates = mapOf(
            "estado" to EstadoOrden.CANCELADO.name,
            "notas" to if (motivoCancelacion.isNotBlank())
                "Cancelado: $motivoCancelacion" else "Pedido cancelado"
        )

        collection
            .document(pedidoId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Para administradores: obtener todos los pedidos
    fun obtenerTodosLosPedidos(
        onSuccess: (List<Orden>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
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
                onSuccess(pedidos)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Obtener pedidos por estado
    fun obtenerPedidosPorEstado(
        estado: EstadoOrden,
        onSuccess: (List<Orden>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .whereEqualTo("estado", estado.name)
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
                onSuccess(pedidos)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Obtener pedidos en un rango de fechas
    fun obtenerPedidosPorFecha(
        userId: String,
        fechaInicio: Long,
        fechaFin: Long,
        onSuccess: (List<Orden>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .whereEqualTo("usuarioId", userId)
            .whereGreaterThanOrEqualTo("fechaCreacion", fechaInicio)
            .whereLessThanOrEqualTo("fechaCreacion", fechaFin)
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
                onSuccess(pedidos)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Escuchar cambios en tiempo real para un usuario específico
    fun escucharPedidosUsuario(
        userId: String,
        onPedidosActualizados: (List<Orden>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .whereEqualTo("usuarioId", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
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
                    onPedidosActualizados(pedidos)
                }
            }
    }

    // Agregar notas a un pedido
    fun agregarNotasPedido(
        pedidoId: String,
        notas: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .document(pedidoId)
            .update("notas", notas)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Obtener estadísticas de pedidos para un usuario
    fun obtenerEstadisticasPedidos(
        userId: String,
        onSuccess: (Map<EstadoOrden, Int>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val estadisticas = mutableMapOf<EstadoOrden, Int>()

                // Inicializar todas las estadísticas en 0
                EstadoOrden.values().forEach { estado ->
                    estadisticas[estado] = 0
                }

                // Contar pedidos por estado
                documents.forEach { document ->
                    try {
                        val orden = document.toObject(Orden::class.java)
                        val estadoActual = estadisticas[orden.estado] ?: 0
                        estadisticas[orden.estado] = estadoActual + 1
                    } catch (e: Exception) {
                        // Ignorar documentos con errores
                    }
                }

                onSuccess(estadisticas)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Buscar pedidos por término de búsqueda (en items)
    fun buscarPedidos(
        userId: String,
        terminoBusqueda: String,
        onSuccess: (List<Orden>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val pedidos = documents.mapNotNull { document ->
                    try {
                        val orden = document.toObject(Orden::class.java).copy(id = document.id)
                        // Filtrar por término de búsqueda en los items
                        if (orden.items.any { item ->
                                item.nombre.contains(terminoBusqueda, ignoreCase = true)
                            }) {
                            orden
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.fechaCreacion }

                onSuccess(pedidos)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}