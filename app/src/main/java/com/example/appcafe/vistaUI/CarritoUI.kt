// vistaUI/CarritoUI.kt
package com.example.appcare.vistaUI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.appcafe.R
import com.example.appcafe.db.Direccion
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.db.MetodoPago
import com.example.appcare.viewModel.CarritoViewModel
import com.example.appcafe.viewModel.DireccionesViewModel
import kotlinx.coroutines.delay


@Composable
fun CarritoUI(
    userId: String,
    viewModel: CarritoViewModel = hiltViewModel(),
    direccionesViewModel: DireccionesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPedidos: () -> Unit // Cambiado de onNavigateToPayment a onNavigateToPedidos
) {
    val carritoState by viewModel.carritoState.collectAsState()
    val direcciones by viewModel.direcciones.collectAsState()

    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showPaymentAnimation by remember { mutableStateOf(false) }

    // Cargar direcciones cuando se inicializa el composable
    LaunchedEffect(userId) {
        viewModel.cargarDirecciones(userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F0E1))
        ) {
            // Header
            CarritoHeader(onNavigateBack = onNavigateBack)

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección de productos
                item {
                    SeccionProductos(
                        items = carritoState.items,
                        onUpdateQuantity = { itemId, newQuantity ->
                            viewModel.actualizarCantidad(itemId, newQuantity)
                        },
                        onRemoveItem = { itemId ->
                            viewModel.eliminarItem(itemId)
                        }
                    )
                }

                // Sección de dirección
                item {
                    SeccionDireccion(
                        direcciones = direcciones,
                        selectedAddress = carritoState.direccionSeleccionada,
                        onSelectAddress = { direccion ->
                            viewModel.seleccionarDireccion(direccion)
                        },
                        onAddNewAddress = {
                            showAddAddressDialog = true
                        }
                    )
                }

                // Sección de método de pago
                item {
                    SeccionMetodoPago(
                        selectedMethod = carritoState.metodoPagoSeleccionado,
                        onSelectMethod = { metodo ->
                            viewModel.seleccionarMetodoPago(metodo)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Bottom section con total y botón
            SeccionInferior(
                total = carritoState.total,
                isEnabled = carritoState.items.isNotEmpty() &&
                        carritoState.direccionSeleccionada != null &&
                        carritoState.metodoPagoSeleccionado != null,
                onPagar = {
                    showPaymentAnimation = true
                }
            )
        }

        // Animación de pago
        if (showPaymentAnimation) {
            PaymentAnimationDialog(
                onAnimationComplete = {
                    showPaymentAnimation = false
                    viewModel.crearOrden()
                    onNavigateToPedidos() // Navegamos a PedidosUI en lugar de PaymentUI
                }
            )
        }
    }

    // Diálogo para agregar nueva dirección
    if (showAddAddressDialog) {
        AgregarDireccionDialog(
            onDismiss = { showAddAddressDialog = false },
            onConfirm = { nombre, descripcion, icono, esPrincipal ->
                direccionesViewModel.agregarDireccion(
                    userId = userId,
                    nombre = nombre,
                    descripcion = descripcion,
                    icono = icono,
                    esPrincipal = esPrincipal,
                    onSuccess = {
                        showAddAddressDialog = false
                        viewModel.cargarDirecciones(userId) // Recargar direcciones
                    },
                    onError = { error ->
                        // Manejar error - podrías mostrar un Snackbar o Toast
                        showAddAddressDialog = false
                    }
                )
            }
        )
    }

    // Manejar errores
    carritoState.error?.let { error ->
        LaunchedEffect(error) {
            // Mostrar snackbar o dialog de error
            viewModel.limpiarError()
        }
    }
}

@Composable
fun PaymentAnimationDialog(
    onAnimationComplete: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.payment_animation)
    )

    // Lanza el efecto para cerrar luego de un tiempo
    LaunchedEffect(Unit) {
        delay(4200)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E1)), // Color de fondo
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(300.dp), // Tamaño más grande, puedes ajustar
                iterations = 1,
                restartOnPlay = false,
                speed = 0.7f
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Pago procesado exitosamente!",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5D4037),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Redirigiendo a tus pedidos...",
                fontSize = 16.sp,
                color = Color(0xFF8D6E63),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarDireccionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var iconoSeleccionado by remember { mutableStateOf("home") }
    var esPrincipal by remember { mutableStateOf(false) }
    var expandedIconos by remember { mutableStateOf(false) }

    val iconosDisponibles = mapOf(
        "home" to Icons.Default.Home,
        "work" to Icons.Default.Work,
        "school" to Icons.Default.School,
        "location" to Icons.Default.LocationOn
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && descripcion.isNotBlank()) {
                        onConfirm(nombre, descripcion, iconoSeleccionado, esPrincipal)
                    }
                },
                enabled = nombre.isNotBlank() && descripcion.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA3A380)
                )
            ) {
                Text("Agregar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF8D6E63))
            }
        },
        title = {
            Text(
                text = "Agregar Nueva Dirección",
                color = Color(0xFF5D4037),
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la dirección") },
                    placeholder = { Text("Ej: Casa, Trabajo, Universidad") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4A574),
                        focusedLabelColor = Color(0xFF8D6E63)
                    )
                )

                // Campo descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Dirección completa") },
                    placeholder = { Text("Ej: Av. Arequipa 123, Miraflores, Lima, Perú") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4A574),
                        focusedLabelColor = Color(0xFF8D6E63)
                    )
                )

                // Selector de icono
                ExposedDropdownMenuBox(
                    expanded = expandedIconos,
                    onExpandedChange = { expandedIconos = !expandedIconos }
                ) {
                    OutlinedTextField(
                        value = when(iconoSeleccionado) {
                            "home" -> "Casa"
                            "work" -> "Trabajo"
                            "school" -> "Universidad"
                            "location" -> "Ubicación"
                            else -> "Casa"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de dirección") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedIconos)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4A574),
                            focusedLabelColor = Color(0xFF8D6E63)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = iconosDisponibles[iconoSeleccionado] ?: Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFFD4A574)
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expandedIconos,
                        onDismissRequest = { expandedIconos = false }
                    ) {
                        iconosDisponibles.forEach { (key, icon) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFFD4A574)
                                        )
                                        Text(
                                            when(key) {
                                                "home" -> "Casa"
                                                "work" -> "Trabajo"
                                                "school" -> "Universidad"
                                                "location" -> "Ubicación"
                                                else -> "Casa"
                                            }
                                        )
                                    }
                                },
                                onClick = {
                                    iconoSeleccionado = key
                                    expandedIconos = false
                                }
                            )
                        }
                    }
                }

                // Checkbox para dirección principal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = esPrincipal,
                        onCheckedChange = { esPrincipal = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFD4A574)
                        )
                    )
                    Text(
                        text = "Establecer como dirección principal",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF8D6E63)
                    )
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CarritoHeader(onNavigateBack: () -> Unit) {
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
            text = "Tu orden:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SeccionProductos(
    items: List<ItemCarrito>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    items.forEach { item ->
        ProductoCard(
            item = item,
            onUpdateQuantity = onUpdateQuantity,
            onRemoveItem = onRemoveItem
        )
    }
}

@Composable
fun ProductoCard(
    item: ItemCarrito,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            val imageUrl = getDirectImageUrl(item.imagenUrl)

            AsyncImage(
                model = imageUrl,
                contentDescription = item.nombre,
                modifier = Modifier
                    .size(60.dp)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4037)
                )

                Text(
                    text = item.tamaño,
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Controles de cantidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón decrementar
                IconButton(
                    onClick = {
                        if (item.cantidad > 1) {
                            onUpdateQuantity(item.id, item.cantidad - 1)
                        } else {
                            onRemoveItem(item.id)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrementar",
                        tint = Color(0xFFD4A574),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cantidad
                Text(
                    text = item.cantidad.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = TextAlign.Center
                )

                // Botón incrementar
                IconButton(
                    onClick = { onUpdateQuantity(item.id, item.cantidad + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Incrementar",
                        tint = Color(0xFFD4A574),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionDireccion(
    direcciones: List<Direccion>,
    selectedAddress: Direccion?,
    onSelectAddress: (Direccion) -> Unit,
    onAddNewAddress: () -> Unit
) {
    Column {
        Text(
            text = "Dirección",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Lista de todas las direcciones disponibles
        direcciones.forEach { direccion ->
            val isSelected = selectedAddress?.id == direccion.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEED2AE),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        Color(0xFF8D6E63) else Color(0xFFFAF3E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = { onSelectAddress(direccion) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getIconForDireccion(direccion.icono),
                        contentDescription = "Ubicación",
                        tint = if (isSelected) Color.White else Color(0xFFD4A574),
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = direccion.nombre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF5D4037)
                        )
                        Text(
                            text = direccion.descripcion,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color(0xFF8D6E63),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Indicador de selección
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Seleccionada",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Botón agregar nueva dirección
        TextButton(
            onClick = onAddNewAddress,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = Color(0xFFD4A574),
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "Agregar una nueva dirección...",
                color = Color(0xFF8D6E63),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SeccionMetodoPago(
    selectedMethod: MetodoPago?,
    onSelectMethod: (MetodoPago) -> Unit
) {
    Column {
        Text(
            text = "Método de pago",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Lista de métodos de pago disponibles
        MetodoPago.values().forEach { metodo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEED2AE),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == metodo)
                        Color(0xFF8D6E63) else Color(0xFFFAF3E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = { onSelectMethod(metodo) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == metodo,
                        onClick = { onSelectMethod(metodo) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (selectedMethod == metodo)
                                Color.White else Color(0xFFD4A574),
                            unselectedColor = Color(0xFFD4A574)
                        )
                    )

                    Icon(
                        imageVector = when(metodo) {
                            MetodoPago.EFECTIVO -> Icons.Default.Money
                        },
                        contentDescription = "Método de pago",
                        tint = if (selectedMethod == metodo)
                            Color.White else Color(0xFFD4A574),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp)
                    )

                    Text(
                        text = metodo.displayName,
                        fontSize = 16.sp,
                        color = if (selectedMethod == metodo)
                            Color.White else Color(0xFF5D4037),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionInferior(
    total: Double,
    isEnabled: Boolean,
    onPagar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFFEED2AE),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .background(
                Color(0xffF5F0E1),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total a pagar: ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "S/${String.format("%.2f", total)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA3A380)
                )
            }

            Button(
                onClick = onPagar,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA3A380),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "Pagar",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

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