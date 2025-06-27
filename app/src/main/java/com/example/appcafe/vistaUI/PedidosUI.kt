package com.example.appcafe.vistaUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.appcafe.R
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.db.Orden
import com.example.appcafe.viewModel.PedidosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidosUI(
    userId: String,
    viewModel: PedidosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val pedidosState by viewModel.pedidosState.collectAsState()

    // Cargar pedidos cuando se inicializa el composable
    LaunchedEffect(userId) {
        viewModel.cargarPedidos(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E1))
    ) {
        // Header
        PedidosHeader(onNavigateBack = onNavigateBack)

        // Content
        when {
            pedidosState.isLoading -> {
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

            pedidosState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar pedidos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8B4513)
                        )
                        Text(
                            text = pedidosState.error!!,
                            fontSize = 14.sp,
                            color = Color(0xFF8D6E63),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            pedidosState.pedidos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes pedidos aún",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8B4513)
                        )
                        Text(
                            text = "Tus pedidos aparecerán aquí una vez que realices tu primera compra",
                            fontSize = 14.sp,
                            color = Color(0xFF8D6E63),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pedidosState.pedidos) { pedido ->
                        PedidoCard(pedido = pedido)
                    }
                }
            }
        }
    }
}

@Composable
fun PedidosHeader(onNavigateBack: () -> Unit) {
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
}

@Composable
fun PedidoCard(pedido: Orden) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido #${pedido.id.take(8)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = formatearFecha(pedido.fechaCreacion),
                        fontSize = 14.sp,
                        color = Color(0xFF8D6E63)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadoBadge(estado = pedido.estado)

                    IconButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Colapsar" else "Expandir",
                            tint = Color(0xFFD4A574)
                        )
                    }
                }
            }

            // Resumen básico
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${pedido.items.size} ${if (pedido.items.size == 1) "producto" else "productos"}",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63)
                )

                Text(
                    text = "S/${String.format("%.2f", pedido.total)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA3A380)
                )
            }

            // Detalles expandibles
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Productos
                    Text(
                        text = "Productos:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    pedido.items.forEach { item ->
                        ProductoEnPedido(item = item)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dirección de entrega
                    Text(
                        text = "Dirección de entrega:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DireccionEnPedido(direccion = pedido.direccionEntrega)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Método de pago
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Money,
                            contentDescription = "Método de pago",
                            tint = Color(0xFFD4A574),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Método de pago: ${pedido.metodoPago}",
                            fontSize = 14.sp,
                            color = Color(0xFF8D6E63),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Fecha estimada de entrega (si existe)
                    if (pedido.fechaEntregaEstimada > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Entrega estimada: ${formatearFecha(pedido.fechaEntregaEstimada)}",
                            fontSize = 14.sp,
                            color = Color(0xFF8D6E63)
                        )
                    }

                    // Notas (si existen)
                    if (pedido.notas.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notas: ${pedido.notas}",
                            fontSize = 14.sp,
                            color = Color(0xFF8D6E63)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total
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
                            text = "S/${String.format("%.2f", pedido.subtotal)}",
                            fontSize = 14.sp,
                            color = Color(0xFF8D6E63)
                        )
                    }

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
                            text = "S/${String.format("%.2f", pedido.total)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA3A380)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: EstadoOrden) {
    val (color, text) = when (estado) {
        EstadoOrden.PENDIENTE -> Color(0xFFFF9800) to "Pendiente"
        EstadoOrden.CONFIRMADO -> Color(0xFF2196F3) to "Confirmado"
        EstadoOrden.EN_PREPARACION -> Color(0xFFFF5722) to "En preparación"
        EstadoOrden.EN_CAMINO -> Color(0xFF9C27B0) to "En camino"
        EstadoOrden.ENTREGADO -> Color(0xFF4CAF50) to "Entregado"
        EstadoOrden.CANCELADO -> Color(0xFFF44336) to "Cancelado"
    }

    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun ProductoEnPedido(item: ItemCarrito) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del producto
        val imageUrl = getDirectImageUrl(item.imagenUrl)

        AsyncImage(
            model = imageUrl,
            contentDescription = item.nombre,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_dialog_alert)
        )

        // Información del producto
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = item.nombre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037)
            )
            Text(
                text = "${item.tamaño} • Cantidad: ${item.cantidad}",
                fontSize = 12.sp,
                color = Color(0xFF8D6E63)
            )
        }

        // Precio
        Text(
            text = "S/${String.format("%.2f", item.precio * item.cantidad)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFA3A380)
        )
    }
}

@Composable
fun DireccionEnPedido(direccion: com.example.appcafe.db.Direccion) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = getIconForDireccion(direccion.icono),
            contentDescription = "Dirección",
            tint = Color(0xFFD4A574),
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = direccion.nombre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037)
            )
            Text(
                text = direccion.descripcion,
                fontSize = 14.sp,
                color = Color(0xFF8D6E63)
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

// Función para obtener el icono correcto según el tipo de dirección
fun getIconForDireccion(iconoTipo: String): ImageVector {
    return when(iconoTipo) {
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        "school" -> Icons.Default.School
        "location" -> Icons.Default.LocationOn
        else -> Icons.Default.Home
    }
}

// Función para obtener URL directa de imagen
fun getDirectImageUrl(url: String): String {
    return when {
        url.startsWith("https://i.imgur.com/") -> url
        url.contains("imgur.com/") -> {
            val id = url.substringAfterLast("/").substringBefore(".")
            "https://i.imgur.com/$id.jpg"
        }
        url.matches(Regex("^[a-zA-Z0-9]+$")) -> {
            "https://i.imgur.com/$url.jpg"
        }
        else -> url
    }
}