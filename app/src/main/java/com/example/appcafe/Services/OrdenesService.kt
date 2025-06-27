package com.example.appcafe.Services

import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
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
    private val auth: FirebaseAuth
) {

    private val userId get() = auth.currentUser?.uid ?: ""
    private val orderCollection = firestore.collection("ordenes")

    suspend fun crearOrden(orden: Orden): String {
        val orderId = UUID.randomUUID().toString()
        val nuevaOrden = orden.copy(
            id = orderId,
            usuarioId = userId
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

    suspend fun getOrdenById(orderId: String): Orden? {
        val snapshot = orderCollection.document(orderId).get().await()
        return snapshot.toObject(Orden::class.java)
    }

    suspend fun actualizarEstadoOrden(orderId: String, status: EstadoOrden) {
        orderCollection.document(orderId).update("estado", status).await()
    }
}