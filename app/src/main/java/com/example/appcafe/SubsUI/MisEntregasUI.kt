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
import kotlinx.coroutines.flow.catch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEntregasUI(
    onNavigateBack: () -> Unit
) {
    val repartidorId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val ordenesService = remember { OrdenesService(firestore = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance(),
        authService = AuthService()) }
    var ordenesAgrupadas by remember { mutableStateOf<Map<String, List<Orden>>>(emptyMap()) }

    LaunchedEffect(repartidorId) {
        ordenesService.getOrdenesPorRepartidor(repartidorId)
            .catch {
                ordenesAgrupadas = emptyMap()
            }
            .collect { ordenes ->
                val agrupadas = ordenes
                    .sortedByDescending { it.fechaCreacion }
                    .groupBy { obtenerEtiquetaFecha(Date(it.fechaCreacion)) }

                ordenesAgrupadas = agrupadas.toSortedMap(compareByDescending { clave ->
                    parseFechaEtiqueta(clave)
                })
            }
    }

    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Entregas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5EDD8))
        ) {
            if (ordenesAgrupadas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no has entregado pedidos.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    ordenesAgrupadas.forEach { (fechaEtiqueta, ordenes) ->
                        item {
                            Text(
                                text = fechaEtiqueta,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color(0xFF3E2C1C)
                            )
                        }

                        items(ordenes) { orden ->
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Cliente: ${orden.usuarioNombre} ${orden.usuarioApellidos}", style = MaterialTheme.typography.titleSmall,color = Color(0xFF6F4837))
                                    Text("Estado: ${orden.estado}", color = Color(0xFF6F4837))
                                    Text("Hora: ${formatoHora.format(orden.fechaCreacion)}",color = Color(0xFF6F4837))
                                    Text("Precio: S/${orden.total}",color = Color(0xFF6F4837))
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
}

private fun obtenerEtiquetaFecha(fecha: Date): String {
    val hoy = Calendar.getInstance()
    val calFecha = Calendar.getInstance().apply { time = fecha }

    return when {
        esMismaFecha(hoy, calFecha) -> "Hoy"
        esAyer(hoy, calFecha) -> "Ayer"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha)
    }
}

private fun esMismaFecha(a: Calendar, b: Calendar): Boolean {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun esAyer(hoy: Calendar, fecha: Calendar): Boolean {
    val ayer = hoy.clone() as Calendar
    ayer.add(Calendar.DAY_OF_YEAR, -1)
    return esMismaFecha(ayer, fecha)
}

private fun parseFechaEtiqueta(etiqueta: String): Date {
    return when (etiqueta) {
        "Hoy" -> Calendar.getInstance().time
        "Ayer" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(etiqueta) ?: Date(0)
    }
}
