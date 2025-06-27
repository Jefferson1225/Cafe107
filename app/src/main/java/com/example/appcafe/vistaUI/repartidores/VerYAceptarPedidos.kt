package com.example.appcafe.vistaUI.repartidores

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.db.Orden
import com.example.appcafe.viewModel.RepartidorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VerYAceptarPedidosUI(
    repartidorId: String,
    viewModel: RepartidorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val pedidosState by viewModel.pedidosState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(repartidorId) {
        viewModel.cargarPedidosParaRepartidor()
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
                text = "Pedidos Disponibles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        when {
            pedidosState.isLoading -> {
                LoadingContent()
            }

            pedidosState.error != null -> {
                ErrorContent(error = pedidosState.error!!)
            }

            pedidosState.pedidos.isEmpty() -> {
                EmptyContent()
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pedidosState.pedidos) { pedido ->
                        PedidoRepartidorCard(
                            pedido = pedido,
                            onAceptarPedido = {
                                viewModel.aceptarPedido(pedido.id, repartidorId)
                            },
                            onEntregarPedido = {
                                viewModel.marcarComoEntregado(pedido.id)
                            },
                            onLlamarCliente = { telefono ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))
                                context.startActivity(intent)
                            }
                        )
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
fun PedidoRepartidorCard(
    pedido: Orden,
    onAceptarPedido: () -> Unit,
    onEntregarPedido: () -> Unit,
    onLlamarCliente: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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

                EstadoRepartidorBadge(estado = pedido.estado)
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            // Botones según el estado
            when (pedido.estado) {
                EstadoOrden.EN_PREPARACION -> {
                    // Botón para aceptar el pedido
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

                EstadoOrden.EN_CAMINO -> {
                    // Mostrar información del cliente y botones de acción
                    Column {
                        // Botón para llamar (simulado - en producción necesitarías el teléfono real)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onLlamarCliente("123456789") }, // Teléfono simulado
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4CAF50)
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
                    }
                }

                else -> {
                    // Para otros estados, mostrar información
                    Text(
                        text = "Estado: ${pedido.estado.name}",
                        fontSize = 14.sp,
                        color = Color(0xFF8D6E63),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoRepartidorBadge(estado: EstadoOrden) {
    val (color, text, icon) = when (estado) {
        EstadoOrden.EN_PREPARACION -> Triple(Color(0xFFFF5722), "Listo", Icons.Default.RestaurantMenu)
        EstadoOrden.EN_CAMINO -> Triple(Color(0xFF9C27B0), "En Camino", Icons.Default.DeliveryDining)
        EstadoOrden.ENTREGADO -> Triple(Color(0xFF4CAF50), "Entregado", Icons.Default.CheckCircle)
        else -> Triple(Color(0xFF8D6E63), estado.name, Icons.Default.Info)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                modifier = Modifier.padding(16.dp)
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
            horizontalAlignment = Alignment.CenterHorizontally
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