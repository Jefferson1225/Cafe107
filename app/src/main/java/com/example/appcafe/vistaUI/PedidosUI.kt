package com.example.appcafe.vistaUI

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import com.example.appcafe.viewModel.PedidosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidosUI(
    userId: String,
    viewModel: PedidosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Título "Tus pedidos:"
                    Text(
                        text = "Tus pedidos:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(pedidosState.pedidos) { pedido ->
                            PedidoCardSimple(
                                pedido = pedido,
                                onClick = { onNavigateToDetalle(pedido.id) }
                            )
                        }
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
fun PedidoCardSimple(
    pedido: Orden,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de delivery
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFF5F0E1),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Delivery",
                    tint = Color(0xFF8B4513),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del pedido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Cafe 107 - Delivery",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4037)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ID: ${pedido.id.take(6)}",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Productos: ${pedido.items.size}",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63)
                )
            }

            // Estado del pedido
            EstadoBadgeSimple(estado = pedido.estado)
        }
    }
}

@Composable
fun EstadoBadgeSimple(estado: EstadoOrden) {
    val (color, text) = when (estado) {
        EstadoOrden.PENDIENTE -> Color(0xFFFF9800) to "Pendiente"
        EstadoOrden.CONFIRMADO -> Color(0xFF2196F3) to "Activo"
        EstadoOrden.EN_PREPARACION -> Color(0xFFFF5722) to "Activo"
        EstadoOrden.ESPERANDO_REPARTIDOR -> Color(0xFF2196F3) to "Activo"
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

// Función para formatear fechas
fun formatearFecha(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}