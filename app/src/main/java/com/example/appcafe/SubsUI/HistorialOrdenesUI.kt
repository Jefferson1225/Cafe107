package com.example.appcafe.SubsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appcafe.Services.OrdenesService
import com.example.appcafe.db.EstadoOrden
import com.example.appcafe.db.Orden
import com.example.cafeteria.db.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialOrdenesUI(onNavigateBack: () -> Unit){
    val ordenesService = remember { OrdenesService(firestore = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance(),
        authService = AuthService()) }
    var ordenes by remember { mutableStateOf<List<Orden>>(emptyList()) }

    LaunchedEffect(Unit) {
        ordenesService.obtenerTodasLasOrdenes(
            onSuccess = { todas ->
                ordenes = todas.filter {
                    it.estado == EstadoOrden.EN_CAMINO || it.estado == EstadoOrden.ENTREGADO
                }.sortedByDescending { it.fechaCreacion } // orden descendente por fecha
            },
            onError = {
                ordenes = emptyList()
            }
        )
    }

    val fechaFormato = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Órdenes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5EDD8))
        ) {
            if (ordenes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay órdenes en camino o entregadas.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(ordenes) { orden ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Cliente: ${orden.usuarioNombre} ${orden.usuarioApellidos}", style = MaterialTheme.typography.titleSmall,color = Color(0xFF6F4837))
                                Text("Estado: ${orden.estado}", color = Color(0xFF6F4837))
                                Text("Fecha: ${fechaFormato.format(orden.fechaCreacion)}",color = Color(0xFF6F4837))
                                orden.items.forEach {
                                    Text("- ${it.nombre} x${it.cantidad}",color = Color(0xFF6F4837))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}