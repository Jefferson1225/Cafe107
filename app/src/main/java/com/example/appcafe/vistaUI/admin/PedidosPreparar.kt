package com.example.appcafe.vistaUI.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.db.Orden
import com.example.appcafe.viewModel.CocinaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PedidosPreparar(
    viewModel: CocinaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val pedidosState by viewModel.pedidosState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPedidosParaCocina()
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
                text = "Pedidos en Preparación",
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
                        PedidoCocinaCard(
                            pedido = pedido,
                            onConfirmarPedido = { viewModel.confirmarPedido(pedido.id) },
                            onPasarARepartidor = { viewModel.pasarPedidoARepartidor(pedido.id) }
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
fun PedidoCocinaCard(
    pedido: Orden,
    onConfirmarPedido: () -> Unit,
    onPasarARepartidor: () -> Unit
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
                        text = "${pedido.usuarioNombre} ${pedido.usuarioApellidos}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF795548)
                    )
                    Text(
                        text = formatearFecha(pedido.fechaCreacion),
                        fontSize = 12.sp,
                        color = Color(0xFF8D6E63)
                    )
                }

                EstadoBadge(estado = pedido.estado)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items del pedido
            Column {
                Text(
                    text = "Productos:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                pedido.items.forEach { item ->
                    ItemResumenRow(item = item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Botón dinámico según el estado
            when (pedido.estado) {
                EstadoOrden.PENDIENTE -> {
                    Button(
                        onClick = onConfirmarPedido,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmar Pedido",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                EstadoOrden.CONFIRMADO -> {
                    Button(
                        onClick = onPasarARepartidor,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listo para Preparar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                EstadoOrden.EN_PREPARACION -> {
                    Button(
                        onClick = onPasarARepartidor,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listo para Entrega",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                else -> {
                    // Para estados EN_CAMINO, ENTREGADO, CANCELADO - solo mostrar estado
                    OutlinedButton(
                        onClick = { /* No action needed */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (pedido.estado) {
                                EstadoOrden.EN_CAMINO -> "En Camino"
                                EstadoOrden.ENTREGADO -> "Entregado"
                                EstadoOrden.CANCELADO -> "Cancelado"
                                else -> "Procesando..."
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemResumenRow(item: ItemCarrito) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${item.cantidad}x ${item.nombre}${if (item.tamaño.isNotEmpty()) " (${item.tamaño})" else ""}",
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

@Composable
fun EstadoBadge(estado: EstadoOrden) {
    val (color, text, icon) = when (estado) {
        EstadoOrden.PENDIENTE -> Triple(Color(0xFFFF9800), "Pendiente", Icons.Default.Schedule)
        EstadoOrden.CONFIRMADO -> Triple(Color(0xFF2196F3), "Confirmado", Icons.Default.CheckCircle)
        EstadoOrden.EN_PREPARACION -> Triple(Color(0xFFFF5722), "Preparando", Icons.Default.Restaurant)
        EstadoOrden.ESPERANDO_REPARTIDOR -> Triple(Color(0xFF2196F3),"Esperando Repa", Icons.Default.AddTask)
        EstadoOrden.EN_CAMINO -> Triple(Color(0xFF9C27B0), "En Camino", Icons.Default.DeliveryDining)
        EstadoOrden.ENTREGADO -> Triple(Color(0xFF4CAF50), "Entregado", Icons.Default.Done)
        EstadoOrden.CANCELADO -> Triple(Color(0xFFF44336), "Cancelado", Icons.Default.Cancel)
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
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = Color(0xFF8D6E63),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay pedidos en preparación",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B4513)
            )
            Text(
                text = "Los nuevos pedidos aparecerán aquí",
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

fun formatearFecha(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/YYYY HH:mm", Locale.getDefault())
    return formatter.format(date)
}