package com.example.cafeteria.db

import android.util.Log
import com.example.appcafe.db.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun iniciarSesion(correo: String, contraseña: String): Result<Usuario> {
        return try {
            val result = auth.signInWithEmailAndPassword(correo, contraseña).await()
            val uid = result.user?.uid ?: throw Exception("UID no encontrado")

            val documento = firestore.collection("usuarios")
                .document(uid)
                .get()
                .await()

            val usuario = documento.toObject(Usuario::class.java)
                ?: throw Exception("Usuario no encontrado en Firestore")

            Result.success(usuario)
        } catch (e: Exception) {
            Log.e("AuthService", "Error al iniciar sesión", e)
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(
        nombre: String,
        apellidos: String,
        correo: String,
        contraseña: String,
        esAdmin: Boolean = false
    ): Result<Usuario> {
        return try {
            val result = auth.createUserWithEmailAndPassword(correo, contraseña).await()
            val uid = result.user?.uid ?: throw Exception("Error al crear usuario")

            val usuario = Usuario(
                id = uid,
                nombre = nombre,
                apellidos = apellidos,
                correo = correo,
                contraseña = "", // No guardamos contraseña
                esAdmin = esAdmin
            )

            firestore.collection("usuarios")
                .document(uid)
                .set(usuario)
                .await()

            Result.success(usuario)
        } catch (e: Exception) {
            Log.e("AuthService", "Error al registrar usuario", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerUsuarioActual(): Usuario? {
        val uid = auth.currentUser?.uid ?: return null
        suspend fun obtenerUsuarioPorId(uid: String): Usuario? {
            return try {
                val documento = firestore.collection("usuarios").document(uid).get().await()
                documento.toObject<Usuario>()
            } catch (e: Exception) {
                null
            }
        }
        return obtenerUsuarioPorId(uid)
    }

    fun esAdmin(): Boolean {

        return runBlocking { obtenerUsuarioActual()?.esAdmin ?: false }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun estaLogueado(): Boolean {
        return auth.currentUser != null
    }
}

// Función para actualizar el teléfono en Firestore
fun actualizarTelefono(uid: String, nuevoTelefono: String) {
    FirebaseFirestore.getInstance()
        .collection("usuarios")
        .document(uid)
        .update("telefono", nuevoTelefono)
}