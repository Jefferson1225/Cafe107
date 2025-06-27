package com.example.appcafe.Services

import com.example.appcafe.db.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductosService {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getProductoPorId(id: String): Producto? {
        return try {
            val doc = firestore.collection("productos").document(id).get().await()
            doc.toObject(Producto::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
}