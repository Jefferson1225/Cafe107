package com.example.appcafe.vistaUI.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcafe.db.Producto
import com.example.appcafe.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoUI(
    onProductoGuardado: () -> Unit = {},
    onCancelar: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel()
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var descripcionCorta by remember { mutableStateOf("") }
    var descripcionLarga by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var expandedCategoria by remember { mutableStateOf(false) }
    var tamañosSeleccionados by remember { mutableStateOf(setOf<String>()) }
    var imagenUrl by remember { mutableStateOf("") }
    var disponible by remember { mutableStateOf(true) }

    // Estados del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    // Lista de categorías disponibles
    val categorias = listOf("Cafes", "Postres", "Desayunos", "Almuerzos")
    val tamañosDisponibles = listOf("Pequeño", "Mediano", "Grande")

    // Observar el estado de operación exitosa
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            // Limpiar formulario
            nombre = ""
            descripcionCorta = ""
            descripcionLarga = ""
            precio = ""
            categoriaSeleccionada = ""
            tamañosSeleccionados = setOf()
            imagenUrl = ""
            disponible = true

            // Limpiar estados del ViewModel
            viewModel.limpiarEstados()

            // Notificar que el producto fue guardado
            onProductoGuardado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)) // Color beige similar a la imagen
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Añadir productos",
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
                // Nombre del Producto
                Text(
                    text = "Nombre del Producto:",
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

                // Descripción Corta
                Text(
                    text = "Descripción Corta:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = descripcionCorta,
                    onValueChange = { descripcionCorta = it },
                    placeholder = { Text("Insertar descripción...") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(bottom = 16.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // Descripción Larga
                Text(
                    text = "Descripción Larga:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = descripcionLarga,
                    onValueChange = { descripcionLarga = it },
                    placeholder = { Text("Insertar descripción...") },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 16.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedContainerColor = Color(0xFFE8E8E0),
                        focusedBorderColor = Color(0xFF8B4513),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedTextColor = Color(0xFF5D2F0C),
                        unfocusedTextColor = Color(0xFF5D2F0C)
                    )
                )

                // URL de Imagen
                Text(
                    text = "URL de Imagen (Imgur):",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = imagenUrl,
                    onValueChange = { imagenUrl = it },
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

                // Fila para Precio y Categoría
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Precio
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Precio:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = precio,
                            onValueChange = {
                                // Solo permitir números y punto decimal
                                if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    precio = it
                                }
                            },
                            placeholder = { Text("0.00") },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFE8E8E0),
                                focusedContainerColor = Color(0xFFE8E8E0),
                                focusedBorderColor = Color(0xFF8B4513),
                                unfocusedBorderColor = Color(0xFFBDBDBD),
                                focusedTextColor = Color(0xFF5D2F0C),
                                unfocusedTextColor = Color(0xFF5D2F0C)
                            )
                        )
                    }

                    // Categoría
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Categoría:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedCategoria,
                            onExpandedChange = {
                                if (!isLoading) {
                                    expandedCategoria = !expandedCategoria
                                }
                            }
                        ) {
                            OutlinedTextField(
                                value = categoriaSeleccionada,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isLoading,
                                placeholder = { Text("Seleccionar...") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFE8E8E0),
                                    focusedContainerColor = Color(0xFFE8E8E0),
                                    focusedBorderColor = Color(0xFF8B4513),
                                    unfocusedBorderColor = Color(0xFFBDBDBD),
                                    focusedTextColor = Color(0xFF5D2F0C),
                                    unfocusedTextColor = Color(0xFF5D2F0C)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedCategoria,
                                onDismissRequest = { expandedCategoria = false }
                            ) {
                                categorias.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria) },
                                        onClick = {
                                            categoriaSeleccionada = categoria
                                            expandedCategoria = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Disponibilidad del producto
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Producto disponible:",
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

                // Tamaños disponibles
                Text(
                    text = "Tamaños disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tamañosDisponibles.forEach { tamaño ->
                        val isSelected = tamañosSeleccionados.contains(tamaño)
                        Button(
                            onClick = {
                                if (!isLoading) {
                                    tamañosSeleccionados = if (isSelected) {
                                        tamañosSeleccionados - tamaño
                                    } else {
                                        tamañosSeleccionados + tamaño
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF8B4513) else Color(0xFFD2B48C),
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tamaño,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Botones de acción con indicador de carga
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.limpiarEstados()
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
                            // Validar campos
                            if (nombre.isNotBlank() &&
                                descripcionCorta.isNotBlank() &&
                                precio.isNotBlank() &&
                                categoriaSeleccionada.isNotBlank()) {

                                val producto = Producto(
                                    id = "", // Firebase generará el ID
                                    nombre = nombre.trim(),
                                    descripcionCorta = descripcionCorta.trim(),
                                    descripcionLarga = descripcionLarga.trim(),
                                    precio = precio.toDoubleOrNull() ?: 0.0,
                                    categoria = categoriaSeleccionada,
                                    tamaños = tamañosSeleccionados.toList(),
                                    imagenUrl = imagenUrl.trim(),
                                    disponible = disponible
                                )

                                viewModel.agregarProducto(producto)
                            } else {
                                // Mostrar error de validación
                                viewModel.limpiarEstados()
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text("Guardando...")
                            }
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}