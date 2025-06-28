package com.example.cafeteria.db

import android.util.Log
import com.example.appcafe.db.Usuario
import com.example.appcafe.db.Repartidor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor() {
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

    // NUEVO: Método para registrar repartidor con cuenta de autenticación
    suspend fun registrarRepartidor(
        nombre: String,
        apellidos: String,
        correo: String,
        contraseña: String,
        telefono: String,
        fotoUrl: String,
        disponible: Boolean = true
    ): Result<Pair<String, String>> { // Retorna UID y ID del repartidor
        return try {
            // 1. Crear cuenta de autenticación
            val result = auth.createUserWithEmailAndPassword(correo, contraseña).await()
            val uid = result.user?.uid ?: throw Exception("Error al crear usuario")

            // 2. Crear usuario en colección usuarios con rol de repartidor
            val usuario = Usuario(
                id = uid,
                nombre = nombre,
                apellidos = apellidos,
                correo = correo,
                contraseña = "", // No guardamos contraseña
                telefono = telefono,
                fotoUrl = fotoUrl,
                esAdmin = false,
                esRepartidor = true // Nuevo campo para identificar repartidores
            )

            firestore.collection("usuarios")
                .document(uid)
                .set(usuario)
                .await()

            // 3. Crear documento de repartidor con el mismo UID
            val repartidor = Repartidor(
                id = uid, // Usar el mismo UID para vincular las cuentas
                nombre = nombre,
                apellidos = apellidos,
                correo = correo,
                contraseña = "", // No guardamos contraseña
                telefono = telefono,
                fotoUrl = fotoUrl,
                disponible = disponible,
                calificacion = 5.0,
                pedidosEntregados = 0
            )

            firestore.collection("repartidores")
                .document(uid)
                .set(repartidor)
                .await()

            Result.success(Pair(uid, uid)) // UID y repartidor ID son iguales
        } catch (e: Exception) {
            Log.e("AuthService", "Error al registrar repartidor", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerUsuarioActual(): Usuario? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val documento = firestore.collection("usuarios").document(uid).get().await()
            documento.toObject<Usuario>()
        } catch (e: Exception) {
            null
        }
    }


    // NUEVO: Obtener datos del repartidor actual
    suspend fun obtenerRepartidorActual(): Repartidor? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val documento = firestore.collection("repartidores").document(uid).get().await()
            documento.toObject<Repartidor>()
        } catch (e: Exception) {
            null
        }
    }

    fun esAdmin(): Boolean {
        return runBlocking { obtenerUsuarioActual()?.esAdmin ?: false }
    }

    // NUEVO: Verificar si es repartidor
    fun esRepartidor(): Boolean {
        return runBlocking { obtenerUsuarioActual()?.esRepartidor ?: false }
    }

    // NUEVO: Obtener tipo de usuario
    suspend fun obtenerTipoUsuario(): TipoUsuario {
        val usuario = obtenerUsuarioActual()
        return when {
            usuario?.esAdmin == true -> TipoUsuario.ADMIN
            usuario?.esRepartidor == true -> TipoUsuario.REPARTIDOR
            usuario != null -> TipoUsuario.CLIENTE
            else -> TipoUsuario.NO_AUTENTICADO
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun estaLogueado(): Boolean {
        return auth.currentUser != null
    }
}

// NUEVO: Enum para tipos de usuario
enum class TipoUsuario {
    ADMIN,
    REPARTIDOR,
    CLIENTE,
    NO_AUTENTICADO
}

// Función para actualizar el teléfono en Firestore
fun actualizarTelefono(uid: String, nuevoTelefono: String) {
    FirebaseFirestore.getInstance()
        .collection("usuarios")
        .document(uid)
        .update("telefono", nuevoTelefono)
}