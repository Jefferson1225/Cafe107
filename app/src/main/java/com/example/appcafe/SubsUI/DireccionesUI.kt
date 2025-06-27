package com.example.appcafe.SubsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appcafe.db.Direccion
import com.example.appcafe.Services.DireccionesService
import com.example.appcafe.viewModel.DireccionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionesUI(
    direcciones: List<Direccion>,
    userId: String,
    onAgregarDireccion: () -> Unit,
    onEditarDireccion: (Direccion) -> Unit,
    onEliminarDireccion: (Direccion) -> Unit,
    onBack: () -> Unit,
    onDireccionAgregada: () -> Unit = {}
) {
    val CafeFondo = Color(0xFFF5EDD8)
    val CafeBorde = Color(0xFFD68C45)
    val CafeTexto = Color(0xFF3E2C1C)

    val viewModel: DireccionesViewModel = hiltViewModel()

    // Estados para diálogo agregar
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var nombreDireccionNueva by remember { mutableStateOf("") }
    var descripcionNueva by remember { mutableStateOf("") }
    var esPrincipalNueva by remember { mutableStateOf(false) }
    var iconoSeleccionadoNombre by remember { mutableStateOf("Home") }

    // Estados para diálogo editar
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var direccionAEditar by remember { mutableStateOf<Direccion?>(null) }
    var nombreDireccionEditar by remember { mutableStateOf("") }
    var descripcionEditar by remember { mutableStateOf("") }
    var esPrincipalEditar by remember { mutableStateOf(false) }
    var iconoSeleccionadoEditar by remember { mutableStateOf("Home") }

    // Estados para diálogo eliminar
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var direccionAEliminar by remember { mutableStateOf<Direccion?>(null) }

    // Estados generales
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val iconosDisponibles = listOf(
        Icons.Default.Home to "Home",
        Icons.Default.Work to "Work",
        Icons.Default.LocationOn to "LocationOn"
    )

    fun obtenerIconoDesdeNombre(nombre: String) = when (nombre) {
        "Home" -> Icons.Default.Home
        "Work" -> Icons.Default.Work
        "LocationOn" -> Icons.Default.LocationOn
        else -> Icons.Default.Home
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeFondo)
    ) {
        TopAppBar(
            title = { Text("Mis direcciones", color = CafeTexto) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CafeTexto)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CafeBorde)
        )

        mensajeError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, CafeBorde, RoundedCornerShape(8.dp))
                .clickable {
                    mostrarDialogoAgregar = true
                    nombreDireccionNueva = ""
                    descripcionNueva = ""
                    esPrincipalNueva = false
                    mensajeError = null
                    iconoSeleccionadoNombre = "Home"
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("+ Agregar Nueva Dirección", color = CafeTexto)
        }

        direcciones.forEach { direccion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(obtenerIconoDesdeNombre(direccion.icono), contentDescription = null, tint = CafeTexto)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(direccion.nombre.ifBlank { "Sin nombre" }, color = CafeTexto)
                            if (direccion.esPrincipal) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Principal",
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(Color.Green, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Text(
                            direccion.descripcion,
                            color = CafeTexto.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = {
                        direccionAEditar = direccion
                        nombreDireccionEditar = direccion.nombre
                        descripcionEditar = direccion.descripcion
                        esPrincipalEditar = direccion.esPrincipal
                        iconoSeleccionadoEditar = direccion.icono
                        mostrarDialogoEditar = true
                        mensajeError = null
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = CafeTexto)
                    }
                    IconButton(onClick = {
                        direccionAEliminar = direccion
                        mostrarDialogoEliminar = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
            }
        }
    }

    // Diálogo Agregar Dirección
    if (mostrarDialogoAgregar) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) mostrarDialogoAgregar = false
            },
            title = { Text("Nueva Dirección", color = CafeTexto) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreDireccionNueva,
                        onValueChange = { nombreDireccionNueva = it },
                        label = { Text("Nombre (ej: Casa, Trabajo)", color = CafeTexto) },
                        placeholder = { Text("Ej: Casa", color = CafeTexto.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CafeTexto,
                            unfocusedTextColor = CafeTexto,
                            focusedLabelColor = CafeTexto,
                            unfocusedLabelColor = CafeTexto.copy(alpha = 0.8f),
                            cursorColor = CafeTexto,
                            focusedBorderColor = CafeBorde,
                            unfocusedBorderColor = CafeBorde
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descripcionNueva,
                        onValueChange = { descripcionNueva = it },
                        label = { Text("Descripción de la dirección", color = CafeTexto) },
                        placeholder = { Text("Ej: Av. Lima 123, San Juan", color = CafeTexto.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CafeTexto,
                            unfocusedTextColor = CafeTexto,
                            focusedLabelColor = CafeTexto,
                            unfocusedLabelColor = CafeTexto.copy(alpha = 0.8f),
                            cursorColor = CafeTexto,
                            focusedBorderColor = Color(0xFF6F4E37),
                            unfocusedBorderColor = CafeBorde
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Selecciona un ícono", color = CafeTexto)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        iconosDisponibles.forEach { (icono, nombre) ->
                            Icon(
                                imageVector = icono,
                                contentDescription = nombre,
                                tint = if (nombre == iconoSeleccionadoNombre) CafeBorde else CafeTexto,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { iconoSeleccionadoNombre = nombre }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = esPrincipalNueva,
                            onCheckedChange = { esPrincipalNueva = it },
                            colors = CheckboxDefaults.colors(checkedColor = CafeBorde)
                        )
                        Text(
                            "Establecer como dirección principal",
                            color = CafeTexto,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (descripcionNueva.isNotBlank()) {
                            cargando = true
                            mensajeError = null
                            viewModel.agregarDireccion(
                                userId = userId,
                                nombre = nombreDireccionNueva.ifBlank { "Dirección sin nombre" },
                                descripcion = descripcionNueva.trim(),
                                icono = iconoSeleccionadoNombre,
                                esPrincipal = esPrincipalNueva,
                                onSuccess = {
                                    cargando = false
                                    mostrarDialogoAgregar = false
                                    onDireccionAgregada()
                                },
                                onError = { error ->
                                    cargando = false
                                    mensajeError = "No se pudo agregar la dirección: ${error.message}"
                                }
                            )
                        }
                    },
                    enabled = descripcionNueva.isNotBlank() && !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeBorde)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Agregar", color = Color(0xFF2D2D2D))
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (!cargando) mostrarDialogoAgregar = false
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeBorde)
                ) {
                    Text("Cancelar", color = Color(0xFF2D2D2D))
                }
            },
            containerColor = Color(0xFFF5F0E1)
        )
    }

    // Diálogo Editar Dirección
    if (mostrarDialogoEditar) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    mostrarDialogoEditar = false
                    direccionAEditar = null
                }
            },
            title = { Text("Editar Dirección", color = CafeTexto) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreDireccionEditar,
                        onValueChange = { nombreDireccionEditar = it },
                        label = { Text("Nombre (ej: Casa, Trabajo)", color = CafeTexto) },
                        placeholder = { Text("Ej: Casa", color = CafeTexto.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CafeTexto,
                            unfocusedTextColor = CafeTexto,
                            focusedLabelColor = CafeTexto,
                            unfocusedLabelColor = CafeTexto.copy(alpha = 0.8f),
                            cursorColor = CafeTexto,
                            focusedBorderColor = CafeBorde,
                            unfocusedBorderColor = CafeBorde
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descripcionEditar,
                        onValueChange = { descripcionEditar = it },
                        label = { Text("Descripción de la dirección", color = CafeTexto) },
                        placeholder = { Text("Ej: Av. Lima 123, San Juan", color = CafeTexto.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CafeTexto,
                            unfocusedTextColor = CafeTexto,
                            focusedLabelColor = CafeTexto,
                            unfocusedLabelColor = CafeTexto.copy(alpha = 0.8f),
                            cursorColor = CafeTexto,
                            focusedBorderColor = Color(0xFF6F4E37),
                            unfocusedBorderColor = CafeBorde
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Selecciona un ícono", color = CafeTexto)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        iconosDisponibles.forEach { (icono, nombre) ->
                            Icon(
                                imageVector = icono,
                                contentDescription = nombre,
                                tint = if (nombre == iconoSeleccionadoEditar) CafeBorde else CafeTexto,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { iconoSeleccionadoEditar = nombre }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = esPrincipalEditar,
                            onCheckedChange = { esPrincipalEditar = it },
                            colors = CheckboxDefaults.colors(checkedColor = CafeBorde)
                        )
                        Text(
                            "Establecer como dirección principal",
                            color = CafeTexto,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        direccionAEditar?.let { direccionOriginal ->
                            if (descripcionEditar.isNotBlank()) {
                                cargando = true
                                mensajeError = null

                                val direccionActualizada = direccionOriginal.copy(
                                    nombre = nombreDireccionEditar.ifBlank { "Dirección sin nombre" },
                                    descripcion = descripcionEditar.trim(),
                                    icono = iconoSeleccionadoEditar,
                                    esPrincipal = esPrincipalEditar
                                )

                                viewModel.actualizarDireccion(
                                    userId = userId,
                                    direccionAntigua = direccionOriginal,
                                    direccionNueva = direccionActualizada,
                                    onSuccess = {
                                        cargando = false
                                        mostrarDialogoEditar = false
                                        direccionAEditar = null
                                        onDireccionAgregada()
                                    },
                                    onError = { error ->
                                        cargando = false
                                        mensajeError = "No se pudo actualizar la dirección: ${error.message}"
                                    }
                                )
                            }
                        }
                    },
                    enabled = descripcionEditar.isNotBlank() && !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeBorde)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Actualizar", color = Color(0xFF2D2D2D))
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (!cargando) {
                            mostrarDialogoEditar = false
                            direccionAEditar = null
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeBorde)
                ) {
                    Text("Cancelar", color = Color(0xFF2D2D2D))
                }
            },
            containerColor = Color(0xFFF5F0E1)
        )
    }

    // Diálogo Confirmar Eliminación
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    mostrarDialogoEliminar = false
                    direccionAEliminar = null
                }
            },
            title = { Text("Confirmar eliminación", color = CafeTexto) },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar la dirección \"${direccionAEliminar?.nombre ?: ""}\"?",
                    color = CafeTexto
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        direccionAEliminar?.let { direccion ->
                            cargando = true
                            mensajeError = null
                            viewModel.eliminarDireccion(
                                userId = userId,
                                direccion = direccion,
                                onSuccess = {
                                    cargando = false
                                    mostrarDialogoEliminar = false
                                    direccionAEliminar = null
                                    onDireccionAgregada()
                                },
                                onError = { error ->
                                    cargando = false
                                    mensajeError = "No se pudo eliminar la dirección: ${error.message}"
                                }
                            )
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Eliminar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (!cargando) {
                            mostrarDialogoEliminar = false
                            direccionAEliminar = null
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeBorde)
                ) {
                    Text("Cancelar", color = Color(0xFF2D2D2D))
                }
            },
            containerColor = Color(0xFFF5F0E1)
        )
    }
}