package com.example.appcafe.db

//modelo de usuario
data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val contraseña: String = "",
    val direcciones: List<Direccion> = emptyList(),
    val favoritos: List<Favorito> = emptyList(),
    val telefono: String = "",
    val esAdmin: Boolean = false,

    val fotoUrl: String = ""
)

//repartidor
data class Repartidor(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val disponible: Boolean = true,
    val calificacion: Double = 5.0,
    val pedidosEntregados: Int = 0
)

//modelo de direccion de usuario
data class Direccion(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val esPrincipal: Boolean = false,
    val icono: String = ""
)

//modelo de producto
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcionCorta: String = "",
    val descripcionLarga: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val tamaños: List<String> = emptyList(),
    val imagenUrl: String = "",
    val disponible: Boolean = true
)

//modelo de favoritos de usuario
data class Favorito(
    val id: String = "",
    val productoId: String = "",
    val nombreProducto: String = "",
    val descripcionProducto: String = "",
    val precioProducto: Double = 0.0,
    val imagenProducto: String = "",
    val categoria: String = "",
    val fechaAgregado: Long = System.currentTimeMillis(),
    val userId: String = ""
)

// Modelo para items del carrito
data class ItemCarrito(
    val id: String = "",
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val tamaño: String = "",
    val imagenUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// Modelo para el carrito
data class Carrito(
    val id: String = "",
    val usuarioId: String = "",
    val items: List<ItemCarrito> = emptyList(),
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)

// Modelo para órdenes/pedidos
data class Orden(
    val id: String = "",
    val usuarioId: String = "",
    val items: List<ItemCarrito> = emptyList(),
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val direccionEntrega: Direccion = Direccion(),
    val metodoPago: String = "",
    val estado: EstadoOrden = EstadoOrden.PENDIENTE,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaEntregaEstimada: Long = 0L,
    val notas: String = "",
    // Nuevos campos para repartidor
    val repartidorId: String = "",
    val repartidorNombre: String = "",
    val repartidorTelefono: String = "",
    val repartidorFoto: String = "",
    val fechaAsignacionRepartidor: Long = 0L
)

// Estados de la orden
enum class EstadoOrden {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}

// Métodos de pago
enum class MetodoPago(val displayName: String) {
    EFECTIVO("Efectivo"),
}