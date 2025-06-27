package com.example.appcafe.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcafe.db.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val productosCollection = db.collection("productos")

    // Estados para manejar el UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    /**
     * Agregar un nuevo producto a Firebase
     */
    fun agregarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                productosCollection
                    .add(producto)
                    .addOnSuccessListener { documentReference ->
                        Log.d("ProductoViewModel", "Producto agregado con ID: ${documentReference.id}")
                        _operacionExitosa.value = true
                        _isLoading.value = false

                        // Actualizar la lista local
                        obtenerProductos()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductoViewModel", "Error agregando producto", e)
                        _errorMessage.value = "Error al agregar producto: ${e.message}"
                        _isLoading.value = false
                        _operacionExitosa.value = false
                    }

            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error inesperado", e)
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
                _operacionExitosa.value = false
            }
        }
    }

    /**
     * Obtener todos los productos de Firebase
     */
    fun obtenerProductos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                productosCollection
                    .get()
                    .addOnSuccessListener { result ->
                        val listaProductos = mutableListOf<Producto>()
                        for (document in result) {
                            try {
                                val producto = document.toObject(Producto::class.java)
                                // Asignar el ID del documento al producto
                                val productoConId = producto.copy(id = document.id)
                                listaProductos.add(productoConId)
                            } catch (e: Exception) {
                                Log.e("ProductoViewModel", "Error convirtiendo documento: ${document.id}", e)
                            }
                        }
                        _productos.value = listaProductos
                        _isLoading.value = false
                        Log.d("ProductoViewModel", "Productos obtenidos: ${listaProductos.size}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductoViewModel", "Error obteniendo productos", e)
                        _errorMessage.value = "Error al obtener productos: ${e.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error inesperado al obtener productos", e)
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualizar un producto existente
     */
    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (producto.id.isBlank()) {
                    _errorMessage.value = "ID del producto no válido"
                    _isLoading.value = false
                    return@launch
                }

                productosCollection
                    .document(producto.id)
                    .set(producto)
                    .addOnSuccessListener {
                        Log.d("ProductoViewModel", "Producto actualizado: ${producto.id}")
                        _operacionExitosa.value = true
                        _isLoading.value = false
                        obtenerProductos()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductoViewModel", "Error actualizando producto", e)
                        _errorMessage.value = "Error al actualizar producto: ${e.message}"
                        _isLoading.value = false
                        _operacionExitosa.value = false
                    }

            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error inesperado al actualizar", e)
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
                _operacionExitosa.value = false
            }
        }
    }

    /**
     * Eliminar un producto
     */
    fun eliminarProducto(productoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                productosCollection
                    .document(productoId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("ProductoViewModel", "Producto eliminado: $productoId")
                        _operacionExitosa.value = true
                        _isLoading.value = false
                        obtenerProductos()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductoViewModel", "Error eliminando producto", e)
                        _errorMessage.value = "Error al eliminar producto: ${e.message}"
                        _isLoading.value = false
                        _operacionExitosa.value = false
                    }

            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error inesperado al eliminar", e)
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
                _operacionExitosa.value = false
            }
        }
    }

    /**
     * Obtener productos por categoría
     */
    fun obtenerProductosPorCategoria(categoria: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                productosCollection
                    .whereEqualTo("categoria", categoria)
                    .get()
                    .addOnSuccessListener { result ->
                        val listaProductos = mutableListOf<Producto>()
                        for (document in result) {
                            try {
                                val producto = document.toObject(Producto::class.java)
                                val productoConId = producto.copy(id = document.id)
                                listaProductos.add(productoConId)
                            } catch (e: Exception) {
                                Log.e("ProductoViewModel", "Error convirtiendo documento: ${document.id}", e)
                            }
                        }
                        _productos.value = listaProductos
                        _isLoading.value = false
                        Log.d("ProductoViewModel", "Productos de categoría '$categoria': ${listaProductos.size}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductoViewModel", "Error obteniendo productos por categoría", e)
                        _errorMessage.value = "Error al filtrar productos: ${e.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error inesperado al filtrar", e)
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpiar mensajes de error y estado de operación exitosa
     */
    fun limpiarEstados() {
        _errorMessage.value = null
        _operacionExitosa.value = false
    }

    /**
     * Cambiar disponibilidad de un producto
     */
    fun cambiarDisponibilidad(productoId: String, disponible: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                productosCollection
                    .document(productoId)
                    .update("disponible", disponible)
                    .addOnSuccessListener {
                        Log.d("ProductoViewModel", "Disponibilidad actualizada: $productoId -> $disponible")
                        _isLoading.value = false
                        obtenerProductos()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductoViewModel", "Error actualizando disponibilidad", e)
                        _errorMessage.value = "Error al cambiar disponibilidad: ${e.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error inesperado", e)
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun mostrarError(mensaje: String) {
        _validationError.value = mensaje
    }
}
