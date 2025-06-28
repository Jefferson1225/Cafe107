package com.example.appcafe.Services

import com.example.appcafe.db.Repartidor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepartidorService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_REPARTIDORES = "repartidores"
    }

    suspend fun agregarRepartidor(repartidor: Repartidor): String {
        return try {
            val docRef = firestore.collection(COLLECTION_REPARTIDORES).document()
            val repartidorConId = repartidor.copy(id = docRef.id)
            docRef.set(repartidorConId).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Error al agregar repartidor: ${e.message}")
        }
    }

    fun getAllRepartidores(): Flow<List<Repartidor>> {
        return firestore.collection(COLLECTION_REPARTIDORES)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Repartidor>()
            }
    }

    fun getRepartidoresDisponibles(): Flow<List<Repartidor>> {
        return firestore.collection(COLLECTION_REPARTIDORES)
            .whereEqualTo("disponible", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Repartidor>()
            }
    }

    suspend fun actualizarDisponibilidad(repartidorId: String, disponible: Boolean) {
        try {
            firestore.collection(COLLECTION_REPARTIDORES)
                .document(repartidorId)
                .update("disponible", disponible)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar disponibilidad: ${e.message}")
        }
    }

    suspend fun actualizarCalificacion(repartidorId: String, nuevaCalificacion: Double) {
        try {
            firestore.collection(COLLECTION_REPARTIDORES)
                .document(repartidorId)
                .update("calificacion", nuevaCalificacion)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar calificación: ${e.message}")
        }
    }

    suspend fun incrementarPedidosEntregados(repartidorId: String) {
        try {
            val docRef = firestore.collection(COLLECTION_REPARTIDORES).document(repartidorId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val pedidosActuales = snapshot.getLong("pedidosEntregados") ?: 0
                transaction.update(docRef, "pedidosEntregados", pedidosActuales + 1)
            }.await()
        } catch (e: Exception) {
            throw Exception("Error al incrementar pedidos entregados: ${e.message}")
        }
    }

    suspend fun getRepartidorById(repartidorId: String): Repartidor? {
        return try {
            val document = firestore.collection(COLLECTION_REPARTIDORES)
                .document(repartidorId)
                .get()
                .await()
            document.toObject(Repartidor::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun eliminarRepartidor(repartidorId: String) {
        try {
            firestore.collection(COLLECTION_REPARTIDORES)
                .document(repartidorId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar repartidor: ${e.message}")
        }
    }

    suspend fun actualizarRepartidor(repartidor: Repartidor) {
        try {
            firestore.collection(COLLECTION_REPARTIDORES)
                .document(repartidor.id)
                .set(repartidor)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar repartidor: ${e.message}")
        }
    }
}