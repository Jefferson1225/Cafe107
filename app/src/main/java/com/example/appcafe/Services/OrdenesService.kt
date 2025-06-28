package com.example.appcafe.Services

import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import com.example.cafeteria.db.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdenesService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val authService: AuthService
) {

    private val userId get() = auth.currentUser?.uid ?: ""
    private val orderCollection = firestore.collection("ordenes")
    private val repartidorCollection = firestore.collection("repartidores")
    private val usuarioCollection = firestore.collection("usuarios")

    suspend fun crearOrden(orden: Orden): String {
        val orderId = UUID.randomUUID().toString()

        val usuario = authService.obtenerUsuarioActual()
        if (usuario == null) throw Exception("No se pudo obtener el usuario actual")

        val nuevaOrden = orden.copy(
            id = orderId,
            usuarioId = usuario.id,
            usuarioNombre = usuario.nombre,
            usuarioApellidos = usuario.apellidos,
            usuarioTelefono = usuario.telefono
        )

        orderCollection.document(orderId).set(nuevaOrden).await()
        return orderId
    }


    fun getOrdenes(): Flow<List<Orden>> = callbackFlow {
        val listener = orderCollection
            .whereEqualTo("usuarioId", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val ordenes = snapshot?.toObjects(Orden::class.java) ?: emptyList()
                trySend(ordenes)
            }

        awaitClose { listener.remove() }
    }

    // Pedidos para cocina, priorizando PENDIENTE -> CONFIRMADO -> EN_PREPARACION
    fun getPedidosParaCocina(): Flow<List<Orden>> = callbackFlow {
        val listener = orderCollection
            .whereIn("estado", listOf(
                EstadoOrden.PENDIENTE.name,
                EstadoOrden.CONFIRMADO.name,
                EstadoOrden.EN_PREPARACION.name
            ))
            .orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val ordenes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Orden::class.java)?.copy(
                        estado = EstadoOrden.valueOf(doc.getString("estado") ?: "PENDIENTE")
                    )
                }?.sortedWith(compareBy({ estadoOrdenPrioridad(it.estado) }, { it.fechaCreacion })) ?: emptyList()

                trySend(ordenes)
            }

        awaitClose { listener.remove() }
    }

    private fun estadoOrdenPrioridad(estado: EstadoOrden): Int {
        return when (estado) {
            EstadoOrden.PENDIENTE -> 0
            EstadoOrden.CONFIRMADO -> 1
            EstadoOrden.EN_PREPARACION -> 2
            else -> 3
        }
    }

    fun getPedidosParaRepartidor(): Flow<List<Orden>> = callbackFlow {
        val listener = orderCollection
            .whereIn("estado", listOf(EstadoOrden.EN_PREPARACION.name, EstadoOrden.EN_CAMINO.name))
            .orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val ordenes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Orden::class.java)?.copy(
                        estado = EstadoOrden.valueOf(doc.getString("estado") ?: "PENDIENTE")
                    )
                } ?: emptyList()

                trySend(ordenes)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getOrdenById(orderId: String): Orden? {
        val snapshot = orderCollection.document(orderId).get().await()
        return snapshot.toObject(Orden::class.java)?.copy(
            estado = EstadoOrden.valueOf(snapshot.getString("estado") ?: "PENDIENTE")
        )
    }

    suspend fun actualizarEstadoOrden(orderId: String, status: EstadoOrden) {
        orderCollection.document(orderId).update("estado", status.name).await()
    }

    suspend fun asignarRepartidor(orderId: String, repartidorId: String) {
        val repartidorDoc = usuarioCollection.document(repartidorId).get().await()
        val repartidor = repartidorDoc.toObject(com.example.appcafe.db.Usuario::class.java)

        if (repartidor != null) {
            val updates = mapOf(
                "repartidorId" to repartidorId,
                "repartidorNombre" to "${repartidor.nombre} ${repartidor.apellidos}",
                "repartidorTelefono" to repartidor.telefono,
                "repartidorFoto" to repartidor.fotoUrl,
                "fechaAsignacionRepartidor" to System.currentTimeMillis()
            )

            orderCollection.document(orderId).update(updates).await()
        }
    }

    suspend fun getOrdenConCliente(orderId: String): Pair<Orden?, com.example.appcafe.db.Usuario?> {
        val orden = getOrdenById(orderId)
        var cliente: com.example.appcafe.db.Usuario? = null

        if (orden != null) {
            val clienteDoc = usuarioCollection.document(orden.usuarioId).get().await()
            cliente = clienteDoc.toObject(com.example.appcafe.db.Usuario::class.java)
        }

        return Pair(orden, cliente)
    }

    suspend fun getRepartidoresDisponibles(): List<com.example.appcafe.db.Usuario> {
        val snapshot = usuarioCollection
            .whereEqualTo("esRepartidor", true)
            .get()
            .await()

        return snapshot.toObjects(com.example.appcafe.db.Usuario::class.java)
    }

    fun getOrdenesPorEstado(estado: EstadoOrden): Flow<List<Orden>> = callbackFlow {
        val listener = orderCollection
            .whereEqualTo("estado", estado.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val ordenes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Orden::class.java)?.copy(
                        estado = EstadoOrden.valueOf(doc.getString("estado") ?: "PENDIENTE")
                    )
                } ?: emptyList()

                trySend(ordenes)
            }

        awaitClose { listener.remove() }
    }

    fun getOrdenesPorRepartidor(repartidorId: String): Flow<List<Orden>> = callbackFlow {
        val listener = orderCollection
            .whereEqualTo("repartidorId", repartidorId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val ordenes = snapshot?.toObjects(Orden::class.java) ?: emptyList()
                trySend(ordenes)
            }

        awaitClose { listener.remove() }
    }


}
