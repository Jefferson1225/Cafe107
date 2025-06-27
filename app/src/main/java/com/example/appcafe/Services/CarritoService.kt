package com.example.appcafe.Services

import com.example.appcafe.db.Carrito
import com.example.appcafe.db.ItemCarrito
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
class CarritoService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val userId get() = auth.currentUser?.uid ?: ""
    private val cartCollection = firestore.collection("carritos")

    fun getCarrito(): Flow<Carrito?> = callbackFlow {
        val listener = cartCollection
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val carrito = snapshot?.toObject(Carrito::class.java)
                trySend(carrito)
            }

        awaitClose { listener.remove() }
    }

    suspend fun agregarItem(item: ItemCarrito) {
        val cartRef = cartCollection.document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            val currentCart = snapshot.toObject(Carrito::class.java) ?: Carrito(id = userId, usuarioId = userId)

            val updatedItems = currentCart.items.toMutableList()
            val existingItemIndex = updatedItems.indexOfFirst {
                it.productoId == item.productoId && it.tamaño == item.tamaño
            }

            if (existingItemIndex >= 0) {
                updatedItems[existingItemIndex] = updatedItems[existingItemIndex].copy(
                    cantidad = updatedItems[existingItemIndex].cantidad + item.cantidad
                )
            } else {
                updatedItems.add(item.copy(id = UUID.randomUUID().toString()))
            }

            val (subtotal, total) = calcularTotales(updatedItems)
            val updatedCart = currentCart.copy(
                items = updatedItems,
                subtotal = subtotal,
                total = total,
                fechaActualizacion = System.currentTimeMillis()
            )

            transaction.set(cartRef, updatedCart)
        }
    }

    suspend fun actualizarCantidad(itemId: String, quantity: Int) {
        val cartRef = cartCollection.document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            val currentCart = snapshot.toObject(Carrito::class.java) ?: return@runTransaction

            val updatedItems = currentCart.items.map { item ->
                if (item.id == itemId) {
                    item.copy(cantidad = quantity)
                } else {
                    item
                }
            }

            val (subtotal, total) = calcularTotales(updatedItems)
            val updatedCart = currentCart.copy(
                items = updatedItems,
                subtotal = subtotal,
                total = total,
                fechaActualizacion = System.currentTimeMillis()
            )

            transaction.set(cartRef, updatedCart)
        }
    }

    suspend fun eliminarItem(itemId: String) {
        val cartRef = cartCollection.document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            val currentCart = snapshot.toObject(Carrito::class.java) ?: return@runTransaction

            val updatedItems = currentCart.items.filter { it.id != itemId }
            val (subtotal, total) = calcularTotales(updatedItems)

            val updatedCart = currentCart.copy(
                items = updatedItems,
                subtotal = subtotal,
                total = total,
                fechaActualizacion = System.currentTimeMillis()
            )

            transaction.set(cartRef, updatedCart)
        }
    }

    suspend fun limpiarCarrito() {
        cartCollection.document(userId).delete()
    }

    private fun calcularTotales(items: List<ItemCarrito>): Pair<Double, Double> {
        val subtotal = items.sumOf { it.precio * it.cantidad }
        val total = subtotal // Aquí puedes agregar impuestos, descuentos, etc.
        return Pair(subtotal, total)
    }
}