package com.example.appcafe.vistaUI.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appcafe.db.Repartidor
import com.example.appcafe.viewModel.RepartidorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarRepartidorUI(
    onRepartidorGuardado: () -> Unit = {},
    onCancelar: () -> Unit = {},
    viewModel: RepartidorViewModel = hiltViewModel()
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var disponible by remember { mutableStateOf(true) }

    // Estados del ViewModel
    val repartidorState by viewModel.pedidosState.collectAsState()
    val isLoading = repartidorState.isLoading
    val errorMessage = repartidorState.error
    val operacionExitosa = repartidorState.operacionExitosa

    // LaunchedEffect para manejar operación exitosa
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            // Limpiar formulario
            nombre = ""
            apellidos = ""
            telefono = ""
            fotoUrl = ""
            disponible = true

            // Limpiar estados del ViewModel
            viewModel.limpiarEstados()

            // Navegar de vuelta
            onRepartidorGuardado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)) // Color beige similar a la imagen
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Añadir Delivery",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Mostrar error si existe
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE6E6))
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Contenido en tarjeta
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0E8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Nombre
                Text(
                    text = "Nombre:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Insertar nombre...") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // Apellidos
                Text(
                    text = "Apellidos:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    placeholder = { Text("Insertar apellidos...") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // Teléfono
                Text(
                    text = "Teléfono:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        // Solo permitir números
                        if (it.all { char -> char.isDigit() }) {
                            telefono = it
                        }
                    },
                    placeholder = { Text("Insertar teléfono...") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // URL de Foto
                Text(
                    text = "URL de Foto (Imgur):",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    placeholder = { Text("https://i.imgur.com/ejemplo.jpg") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // Disponibilidad del repartidor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disponible:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = disponible,
                        onCheckedChange = {
                            if (!isLoading) {
                                disponible = it
                            }
                        },
                        enabled = !isLoading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8B4513),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDBDBD)
                        )
                    )

                    Text(
                        text = if (disponible) "Sí" else "No",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (disponible) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.limpiarError()
                            onCancelar()
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD2B48C),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            // Validar campos obligatorios
                            if (nombre.isNotBlank() &&
                                apellidos.isNotBlank() &&
                                telefono.isNotBlank()) {

                                val repartidor = Repartidor(
                                    id = "", // Firebase generará el ID
                                    nombre = nombre.trim(),
                                    apellidos = apellidos.trim(),
                                    telefono = telefono.trim(),
                                    fotoUrl = fotoUrl.trim().ifEmpty { "" },
                                    disponible = disponible,
                                    calificacion = 5.0, // Calificación inicial
                                    pedidosEntregados = 0 // Pedidos entregados inicial
                                )

                                // Llamar al método del ViewModel para agregar repartidor
                                viewModel.agregarRepartidor(repartidor)

                            } else {
                                // Mostrar error de validación
                                val camposFaltantes = mutableListOf<String>()
                                if (nombre.isBlank()) camposFaltantes.add("Nombre")
                                if (apellidos.isBlank()) camposFaltantes.add("Apellidos")
                                if (telefono.isBlank()) camposFaltantes.add("Teléfono")

                                viewModel.mostrarError("Campos obligatorios faltantes: ${camposFaltantes.joinToString(", ")}")
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B4513),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text("Guardando...")
                            }
                        } else {
                            Text("Registrar")
                        }
                    }
                }
            }
        }
    }
}