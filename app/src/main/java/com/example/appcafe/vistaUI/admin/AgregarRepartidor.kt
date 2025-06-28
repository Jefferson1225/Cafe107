package com.example.appcafe.vistaUI.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var disponible by remember { mutableStateOf(true) }

    // Estados para mostrar/ocultar contraseñas
    var mostrarContraseña by remember { mutableStateOf(false) }
    var mostrarConfirmarContraseña by remember { mutableStateOf(false) }

    // Estado para mostrar alerta de éxito
    var mostrarAlertaExito by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val repartidorState by viewModel.pedidosState.collectAsState()
    val isLoading = repartidorState.isLoading
    val errorMessage = repartidorState.error
    val operacionExitosa = repartidorState.operacionExitosa

    // LaunchedEffect para manejar operación exitosa
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            // Mostrar alerta de éxito
            mostrarAlertaExito = true
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

                // Correo electrónico
                Text(
                    text = "Correo electrónico:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("ejemplo@correo.com") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // Contraseña
                Text(
                    text = "Contraseña:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = contraseña,
                    onValueChange = { contraseña = it },
                    placeholder = { Text("Mínimo 6 caracteres") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    visualTransformation = if (mostrarContraseña) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarContraseña = !mostrarContraseña }) {
                            Icon(
                                imageVector = if (mostrarContraseña) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (mostrarContraseña) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // Confirmar contraseña
                Text(
                    text = "Confirmar contraseña:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = confirmarContraseña,
                    onValueChange = { confirmarContraseña = it },
                    placeholder = { Text("Repetir contraseña") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    visualTransformation = if (mostrarConfirmarContraseña) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarConfirmarContraseña = !mostrarConfirmarContraseña }) {
                            Icon(
                                imageVector = if (mostrarConfirmarContraseña) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (mostrarConfirmarContraseña) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    isError = confirmarContraseña.isNotEmpty() && contraseña != confirmarContraseña,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C),
                        errorBorderColor = Color.Red
                    )
                )

                // Mensaje de error para contraseñas diferentes
                if (confirmarContraseña.isNotEmpty() && contraseña != confirmarContraseña) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
                    )
                }

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
                            val camposFaltantes = mutableListOf<String>()
                            if (nombre.isBlank()) camposFaltantes.add("Nombre")
                            if (apellidos.isBlank()) camposFaltantes.add("Apellidos")
                            if (correo.isBlank()) camposFaltantes.add("Correo")
                            if (contraseña.isBlank()) camposFaltantes.add("Contraseña")
                            if (telefono.isBlank()) camposFaltantes.add("Teléfono")

                            // Validaciones específicas
                            when {
                                camposFaltantes.isNotEmpty() -> {
                                    viewModel.mostrarError("Campos obligatorios faltantes: ${camposFaltantes.joinToString(", ")}")
                                }
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                                    viewModel.mostrarError("El formato del correo electrónico no es válido")
                                }
                                contraseña.length < 6 -> {
                                    viewModel.mostrarError("La contraseña debe tener al menos 6 caracteres")
                                }
                                contraseña != confirmarContraseña -> {
                                    viewModel.mostrarError("Las contraseñas no coinciden")
                                }
                                telefono.length < 9 -> {
                                    viewModel.mostrarError("El número de teléfono debe tener al menos 9 dígitos")
                                }
                                else -> {
                                    val repartidor = Repartidor(
                                        id = "", // Firebase generará el ID
                                        nombre = nombre.trim(),
                                        apellidos = apellidos.trim(),
                                        correo = correo.trim().lowercase(),
                                        contraseña = contraseña, // En producción deberías hashear esto
                                        telefono = telefono.trim(),
                                        fotoUrl = fotoUrl.trim().ifEmpty { "" },
                                        disponible = disponible,
                                        calificacion = 5.0, // Calificación inicial
                                        pedidosEntregados = 0 // Pedidos entregados inicial
                                    )

                                    // Llamar al método del ViewModel para agregar repartidor
                                    viewModel.agregarRepartidor(repartidor)
                                }
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

        // AlertDialog de éxito
        if (mostrarAlertaExito) {
            AlertDialog(
                onDismissRequest = { /* No permitir cerrar tocando fuera */ },
                title = {
                    Text(
                        text = "¡Éxito!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF2E7D32)
                    )
                },
                text = {
                    Text(
                        text = "Repartidor registrado correctamente",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarAlertaExito = false

                            // Limpiar formulario
                            nombre = ""
                            apellidos = ""
                            correo = ""
                            contraseña = ""
                            confirmarContraseña = ""
                            telefono = ""
                            fotoUrl = ""
                            disponible = true

                            // Limpiar estados del ViewModel
                            viewModel.limpiarEstados()

                            // Navegar de vuelta
                            onRepartidorGuardado()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B4513),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Aceptar")
                    }
                },
                containerColor = Color.White,
                titleContentColor = Color(0xFF2E7D32),
                textContentColor = Color.Black
            )
        }
    }
}