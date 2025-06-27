package com.example.appcafe.Services

import com.example.appcafe.db.Direccion
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DireccionesService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getDirecciones(userId: String): Flow<List<Direccion>> = callbackFlow {
        val listener = firestore.collection("usuarios")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val direcciones = snapshot.get("direcciones") as? List<Map<String, Any>> ?: emptyList()
                    val direccionesList = direcciones.mapNotNull { direccionMap ->
                        try {
                            Direccion(
                                id = direccionMap["id"] as? String ?: "",
                                nombre = direccionMap["nombre"] as? String ?: "",
                                descripcion = direccionMap["descripcion"] as? String ?: "",
                                icono = direccionMap["icono"] as? String ?: "Home",
                                esPrincipal = direccionMap["esPrincipal"] as? Boolean ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(direccionesList)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    fun agregarDireccion(
        userId: String,
        nombre: String,
        descripcion: String,
        icono: String = "Home",
        esPrincipal: Boolean = false,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nuevaDireccion = Direccion(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            descripcion = descripcion,
            icono = icono,
            esPrincipal = esPrincipal
        )

        firestore.collection("usuarios")
            .document(userId)
            .update("direcciones", FieldValue.arrayUnion(nuevaDireccion))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }

    fun eliminarDireccion(
        userId: String,
        direccion: Direccion,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("usuarios")
            .document(userId)
            .update("direcciones", FieldValue.arrayRemove(direccion))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }

    fun actualizarDireccion(
        userId: String,
        direccionAntigua: Direccion,
        direccionNueva: Direccion,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = firestore.collection("usuarios").document(userId)

        firestore.runTransaction { transaction ->
            transaction.update(userRef, "direcciones", FieldValue.arrayRemove(direccionAntigua))
            transaction.update(userRef, "direcciones", FieldValue.arrayUnion(direccionNueva))
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onError(exception)
        }
    }
}
