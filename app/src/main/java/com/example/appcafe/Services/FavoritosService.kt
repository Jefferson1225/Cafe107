package com.example.appcafe.Services

import com.google.firebase.firestore.FirebaseFirestore
import com.example.appcafe.db.Favorito
import java.util.UUID

class FavoritosService {
    private val firestore = FirebaseFirestore.getInstance()

    // Agregar producto a favoritos
    fun agregarFavorito(
        userId: String,
        productoId: String,
        nombreProducto: String,
        descripcionProducto: String,
        precioProducto: Double,
        imagenProducto: String = "",
        categoria: String = "",
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val favoritoId = UUID.randomUUID().toString()
        val nuevoFavorito = Favorito(
            id = favoritoId,
            productoId = productoId,
            nombreProducto = nombreProducto,
            descripcionProducto = descripcionProducto,
            precioProducto = precioProducto,
            imagenProducto = imagenProducto,
            categoria = categoria,
            fechaAgregado = System.currentTimeMillis(),
            userId = userId
        )

        firestore.collection("usuarios")
            .document(userId)
            .collection("favoritos")
            .document(favoritoId)
            .set(nuevoFavorito)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }

    // Eliminar favorito
    fun eliminarFavorito(
        userId: String,
        favoritoId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("usuarios")
            .document(userId)
            .collection("favoritos")
            .document(favoritoId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }

    // Verificar si un producto está en favoritos
    fun esFavorito(
        userId: String,
        productoId: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("usuarios")
            .document(userId)
            .collection("favoritos")
            .whereEqualTo("productoId", productoId)
            .get()
            .addOnSuccessListener { result ->
                onResult(!result.isEmpty)
            }
            .addOnFailureListener { exception -> onError(exception) }
    }

    // Obtener todos los favoritos del usuario
    fun obtenerFavoritos(
        userId: String,
        onSuccess: (List<Favorito>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("usuarios")
            .document(userId)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { result ->
                val favoritos = result.mapNotNull { it.toObject(Favorito::class.java) }
                onSuccess(favoritos)
            }
            .addOnFailureListener { exception -> onError(exception) }
    }
}
