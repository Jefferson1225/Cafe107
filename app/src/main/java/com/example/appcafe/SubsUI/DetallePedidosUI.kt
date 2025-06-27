package com.example.appcafe.SubsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.db.Orden
import com.example.appcafe.viewModel.DetallePedidoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetallePedidosUI(
    pedidoId: String,
    viewModel: DetallePedidoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val pedidoState by viewModel.pedidoState.collectAsState()

    // Cargar el pedido cuando se inicializa
    LaunchedEffect(pedidoId) {
        viewModel.cargarPedido(pedidoId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E1))
    ) {
        // Header
        DetallePedidoHeader(
            onNavigateBack = onNavigateBack,
            pedido = pedidoState.pedido
        )

        when {
            pedidoState.isLoading -> {
                LoadingContent()
            }

            pedidoState.error != null -> {
                ErrorContent(error = pedidoState.error!!)
            }

            pedidoState.pedido != null -> {
                DetallePedidoContent(pedido = pedidoState.pedido!!)
            }
        }
    }
}

@Composable
fun DetallePedidoHeader(
    onNavigateBack: () -> Unit,
    pedido: Orden?
) {
    Column {
        // Top bar con botón back
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
                text = "Detalle del Pedido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Card principal con información del pedido
        if (pedido != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Título y estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cafe 107 - Delivery",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )

                        EstadoBadge(estado = pedido.estado)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ID del pedido
                    Text(
                        text = "ID: ${pedido.id.take(6)}",
                        fontSize = 14.sp,
                        color = Color(0xFF8D6E63)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fecha
                    Text(
                        text = formatearFecha(pedido.fechaCreacion),
                        fontSize = 14.sp,
                        color = Color(0xFF8D6E63)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progreso del pedido
                    ProgressoPedido(estado = pedido.estado)
                }
            }
        }
    }
}

@Composable
fun DetallePedidoContent(pedido: Orden) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Items del pedido
        item {
            ItemsPedidoSection(items = pedido.items)
        }

        // Información del repartidor (solo si está asignado)
        if (pedido.repartidorId.isNotEmpty()) {
            item {
                RepartidorSection(pedido = pedido)
            }
        }

        // Dirección de entrega
        item {
            DireccionEntregaSection(direccion = pedido.direccionEntrega)
        }

        // Resumen de pago
        item {
            ResumenPagoSection(pedido = pedido)
        }

        // Espacio final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


//repartidor section
@Composable
fun RepartidorSection(pedido: Orden) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tu Repartidor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del repartidor
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (pedido.repartidorFoto.isNotEmpty()) {
                        // Aquí podrías cargar la imagen real con Coil o similar
                        // AsyncImage(model = pedido.repartidorFoto, ...)
                        Text(
                            text = pedido.repartidorNombre.firstOrNull()?.toString() ?: "R",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = pedido.repartidorNombre.firstOrNull()?.toString() ?: "R",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pedido.repartidorNombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5D4037)
                    )

                    Text(
                        text = "Repartidor asignado",
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63)
                    )

                    if (pedido.estado == EstadoOrden.EN_CAMINO) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Color(0xFF4CAF50),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En camino hacia ti",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Botón para llamar al repartidor
                if (pedido.repartidorTelefono.isNotEmpty() && pedido.estado == EstadoOrden.EN_CAMINO) {
                    OutlinedButton(
                        onClick = {
                            // Aquí implementar la llamada
                            // val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${pedido.repartidorTelefono}"))
                            // context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Llamar repartidor",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun ItemsPedidoSection(items: List<ItemCarrito>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Productos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEach { item ->
                ItemPedidoRow(item = item)
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ItemPedidoRow(item: ItemCarrito) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${item.cantidad}x ${item.nombre}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037)
            )
            if (item.tamaño.isNotEmpty()) {
                Text(
                    text = item.tamaño,
                    fontSize = 12.sp,
                    color = Color(0xFF8D6E63)
                )
            }
        }

        Text(
            text = "S/${String.format("%.0f", item.precio * item.cantidad)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D4037)
        )
    }
}

@Composable
fun DireccionEntregaSection(direccion: com.example.appcafe.db.Direccion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = direccion.descripcion.ifEmpty { "Dirección de entrega" },
                    fontSize = 14.sp,
                    color = Color(0xFF5D4037)
                )
            }
        }
    }
}

@Composable
fun ResumenPagoSection(pedido: Orden) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal:",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63)
                )
                Text(
                    text = "S/${String.format("%.0f", pedido.subtotal)}",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "S/${String.format("%.0f", pedido.total)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
            }
        }
    }
}

@Composable
fun ProgressoPedido(estado: EstadoOrden) {
    Column {
        val progress = when (estado) {
            EstadoOrden.PENDIENTE -> 0f  // Cambiado de 0.2f a 0f
            EstadoOrden.CONFIRMADO -> 0.25f  // Ajustado para que comience aquí
            EstadoOrden.EN_PREPARACION -> 0.5f
            EstadoOrden.EN_CAMINO -> 0.75f
            EstadoOrden.ENTREGADO -> 1f
            EstadoOrden.CANCELADO -> 0f
        }

        Text(
            text = "Estado del pedido",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (estado != EstadoOrden.CANCELADO) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estados mejorados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EstadoItem(
                    texto = "Confirmado",
                    activo = progress >= 0.25f,  // Ajustado para que se active con CONFIRMADO
                    icono = Icons.Default.CheckCircle
                )
                EstadoItem(
                    texto = "Preparando",
                    activo = progress >= 0.5f,
                    icono = Icons.Default.Restaurant
                )
                EstadoItem(
                    texto = "En camino",
                    activo = progress >= 0.75f,
                    icono = Icons.Default.DeliveryDining
                )
                EstadoItem(
                    texto = "Entregado",
                    activo = progress >= 1f,
                    icono = Icons.Default.Home
                )
            }
        } else {
            // Estado cancelado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pedido cancelado",
                        fontSize = 14.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoItem(
    texto: String,
    activo: Boolean,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (activo) Color(0xFF4CAF50) else Color(0xFF8D6E63),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = texto,
            fontSize = 10.sp,
            color = if (activo) Color(0xFF4CAF50) else Color(0xFF8D6E63),
            fontWeight = if (activo) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun EstadoBadge(estado: EstadoOrden) {
    val (color, text) = when (estado) {
        EstadoOrden.PENDIENTE -> Color(0xFFFF9800) to "Pendiente"
        EstadoOrden.CONFIRMADO -> Color(0xFF2196F3) to "Activo"
        EstadoOrden.EN_PREPARACION -> Color(0xFFFF5722) to "Activo"
        EstadoOrden.EN_CAMINO -> Color(0xFF9C27B0) to "Activo"
        EstadoOrden.ENTREGADO -> Color(0xFF4CAF50) to "Entregado"
        EstadoOrden.CANCELADO -> Color(0xFFF44336) to "Cancelado"
    }

    Box(
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
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
                "Cargando detalles...",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error al cargar el pedido",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B4513)
            )
            Text(
                text = error,
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Función para formatear fechas
fun formatearFecha(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}


