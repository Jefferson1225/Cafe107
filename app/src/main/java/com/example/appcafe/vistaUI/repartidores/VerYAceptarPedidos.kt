package com.example.appcafe.vistaUI.repartidores

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition.Center.position
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import com.example.appcafe.viewModel.PedidosViewModel
import com.example.appcafe.viewModel.obtenerCoordenadasDesdeDireccion
import com.example.cafeteria.db.AuthService
import com.google.maps.android.compose.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VerYAceptarPedidosUI(
    repartidorId: String,
    pedidosViewModel: PedidosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val authService = remember { AuthService() }

    // Estados para pedidos y repartidor
    var pedidosDisponibles by remember { mutableStateOf<List<Orden>>(emptyList()) }
    var pedidosEnCamino by remember { mutableStateOf<List<Orden>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cargar pedidos al iniciar
    LaunchedEffect(repartidorId) {
        try {
            isLoading = true
            error = null

            // Cargar pedidos listos para entrega (EN_PREPARACION)
            pedidosViewModel.cargarPedidosPorEstado(EstadoOrden.ESPERANDO_REPARTIDOR) { pedidos ->
                pedidosDisponibles = pedidos
            }

            // Cargar pedidos asignados al repartidor (EN_CAMINO)
            pedidosViewModel.cargarPedidosDeRepartidor(repartidorId) { pedidos ->
                pedidosEnCamino = pedidos.filter { it.estado == EstadoOrden.EN_CAMINO }
            }

            isLoading = false
        } catch (e: Exception) {
            error = "Error al cargar pedidos: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E1))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF8B4513)
                )
            }

            Text(
                text = "Mis Pedidos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        when {
            isLoading -> {
                LoadingContent()
            }

            error != null -> {
                ErrorContent(error = error!!)
            }

            pedidosDisponibles.isEmpty() && pedidosEnCamino.isEmpty() -> {
                EmptyContent()
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sección de pedidos en camino (mis entregas activas)
                    if (pedidosEnCamino.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Mis Entregas Activas",
                                icon = Icons.Default.DeliveryDining,
                                count = pedidosEnCamino.size
                            )
                        }

                        items(pedidosEnCamino) { pedido ->
                            PedidoRepartidorCard(
                                pedido = pedido,
                                esPropio = true,
                                onAceptarPedido = {
                                    if (pedidosEnCamino.isNotEmpty()) {
                                        error = "Solo puedes tener un pedido activo a la vez."
                                    } else {
                                        pedidosViewModel.asignarRepartidor(
                                            pedidoId = pedido.id,
                                            repartidorId = repartidorId,
                                            onSuccess = {
                                                // Recargar ambas listas
                                                pedidosViewModel.cargarPedidosPorEstado(EstadoOrden.ESPERANDO_REPARTIDOR) { pedidos ->
                                                    pedidosDisponibles = pedidos
                                                }
                                                pedidosViewModel.cargarPedidosDeRepartidor(repartidorId) { pedidos ->
                                                    pedidosEnCamino = pedidos.filter { it.estado == EstadoOrden.EN_CAMINO }
                                                }
                                            },
                                            onError = { errorMsg ->
                                                error = errorMsg
                                            }
                                        )
                                    }
                                }
                                ,
                                onEntregarPedido = {
                                    pedidosViewModel.marcarComoEntregado(
                                        pedidoId = pedido.id,
                                        onSuccess = {
                                            // Recargar pedidos
                                            pedidosViewModel.cargarPedidosDeRepartidor(repartidorId) { pedidos ->
                                                pedidosEnCamino = pedidos.filter { it.estado == EstadoOrden.EN_CAMINO }
                                            }
                                        },
                                        onError = { errorMsg ->
                                            error = errorMsg
                                        }
                                    )
                                },
                                onLlamarUsuario = { telefono ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))
                                    context.startActivity(intent)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Sección de pedidos disponibles para tomar
                    if (pedidosDisponibles.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Pedidos Disponibles",
                                icon = Icons.Default.LocalShipping,
                                count = pedidosDisponibles.size
                            )
                        }

                        items(pedidosDisponibles) { pedido ->
                            PedidoRepartidorCard(
                                pedido = pedido,
                                esPropio = false,
                                onAceptarPedido = {
                                    pedidosViewModel.asignarRepartidor(
                                        pedidoId = pedido.id,
                                        repartidorId = repartidorId,
                                        onSuccess = {
                                            // Recargar ambas listas
                                            pedidosViewModel.cargarPedidosPorEstado(EstadoOrden.ESPERANDO_REPARTIDOR) { pedidos ->
                                                pedidosDisponibles = pedidos
                                            }
                                            pedidosViewModel.cargarPedidosDeRepartidor(repartidorId) { pedidos ->
                                                pedidosEnCamino = pedidos.filter { it.estado == EstadoOrden.EN_CAMINO }
                                            }
                                        },
                                        onError = { errorMsg ->
                                            error = errorMsg
                                        }
                                    )
                                }
                                ,
                                onEntregarPedido = { /* No aplica para pedidos no asignados */ },
                                onLlamarUsuario = { telefono ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8B4513)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun PedidoRepartidorCard(
    pedido: Orden,
    esPropio: Boolean, // Si el pedido ya está asignado al repartidor
    onAceptarPedido: () -> Unit,
    onEntregarPedido: () -> Unit,
    onLlamarUsuario: (String) -> Unit // CAMBIADO: onLlamarCliente -> onLlamarUsuario
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esPropio) Color(0xFFF0F8FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido #${pedido.id.take(6)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = formatearFecha(pedido.fechaCreacion),
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63)
                    )
                }

                EstadoRepartidorBadge(
                    estado = pedido.estado,
                    esPropio = esPropio
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información del usuario (solo si es propio)
            if (esPropio && pedido.estado == EstadoOrden.EN_CAMINO) {
                val context = LocalContext.current
                var coordenadas by remember { mutableStateOf<Pair<Double, Double>?>(null) }

                LaunchedEffect(pedido.id) {
                    coordenadas = obtenerCoordenadasDesdeDireccion(context, pedido.direccionEntrega.descripcion)
                }

                Spacer(modifier = Modifier.height(16.dp))

                coordenadas?.let { (lat, lon) ->
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(lat, lon), 16f)
                    }

                    // Información del usuario
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Usuario",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "${pedido.usuarioNombre} ${pedido.usuarioApellidos}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = LatLng(lat, lon)),
                            title = "Destino del cliente"
                        )
                    }
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${lat},${lon}?q=${lat},${lon}(Destino del cliente)")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver en Google Maps", color = Color.White)
                    }

                }
            }


            // Dirección
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = Color(0xFF8B4513),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Dirección de entrega:",
                            fontSize = 12.sp,
                            color = Color(0xFF8D6E63)
                        )
                        Text(
                            text = pedido.direccionEntrega.descripcion,
                            fontSize = 14.sp,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items resumidos
            Text(
                text = "Productos (${pedido.items.size} items):",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(8.dp))

            pedido.items.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.cantidad}x ${item.nombre}",
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "S/${String.format("%.0f", item.precio * item.cantidad)}",
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63)
                    )
                }
            }

            if (pedido.items.size > 3) {
                Text(
                    text = "... y ${pedido.items.size - 3} productos más",
                    fontSize = 12.sp,
                    color = Color(0xFF8D6E63),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8F0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total a cobrar:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = "S/${String.format("%.0f", pedido.total)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones según el estado y si es propio
            if (esPropio && pedido.estado == EstadoOrden.EN_CAMINO) {
                // Pedido asignado al repartidor - mostrar botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onLlamarUsuario(pedido.usuarioTelefono) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Llamar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onEntregarPedido,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Entregar", fontSize = 12.sp)
                    }
                }
            } else if (!esPropio && pedido.estado == EstadoOrden.ESPERANDO_REPARTIDOR) {
                // Pedido disponible para tomar
                Button(
                    onClick = onAceptarPedido,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aceptar Entrega",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoRepartidorBadge(
    estado: EstadoOrden,
    esPropio: Boolean = false
) {
    val (color, text, icon) = when {
        estado == EstadoOrden.EN_PREPARACION && !esPropio ->
            Triple(Color(0xFFFF5722), "Disponible", Icons.Default.LocalShipping)
        estado == EstadoOrden.EN_CAMINO && esPropio ->
            Triple(Color(0xFF9C27B0), "Mi Entrega", Icons.Default.DeliveryDining)
        estado == EstadoOrden.ENTREGADO ->
            Triple(Color(0xFF4CAF50), "Entregado", Icons.Default.CheckCircle)
        else ->
            Triple(Color(0xFF8D6E63), estado.name, Icons.Default.Info)
    }

    Row(
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFFD68C45)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Cargando pedidos...",
                color = Color(0xFF3E2C1C),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ErrorContent(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error al cargar pedidos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B4513)
            )
            Text(
                text = error,
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeliveryDining,
                contentDescription = null,
                tint = Color(0xFF8D6E63),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay pedidos disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B4513)
            )
            Text(
                text = "Los pedidos listos para entrega aparecerán aquí",
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

fun formatearFecha(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return formatter.format(date)
}